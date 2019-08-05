package net.nextabc.edgex;

import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 陈永佳 (yoojiachen@gmail.com)
 * @version 0.0.1
 */
final class DriverImpl implements Driver {

    private static final Logger log = Logger.getLogger(DriverImpl.class);

    private final Globals globals;

    private final Timer statsTimer;

    private final String[] mqttTopics;
    private final Stats stats = new Stats();

    private final AtomicInteger sequenceId = new AtomicInteger(0);
    private final List<OnStartupListener<Driver>> startupListeners = new ArrayList<>(0);
    private final List<OnShutdownListener<Driver>> shutdownListeners = new ArrayList<>(0);
    private final Router router = new Router();

    private final MqttClient mqttClientRef;
    private final String nodeId;

    private DriverHandler handler;

    DriverImpl(String nodeId, MqttClient mqttClient, Globals globals, Options options) {
        this.nodeId = nodeId;
        this.mqttClientRef = mqttClient;
        this.globals = Objects.requireNonNull(globals);
        this.statsTimer = new Timer("DriverStatsTimer");
        // topics
        final List<String> topics = new ArrayList<>();
        for (String topic : options.eventTopics) {
            topics.add(Topics.formatEvents(topic));
        }
        for (String topic : options.valueTopics) {
            topics.add(Topics.formatValues(topic));
        }
        topics.addAll(Arrays.asList(options.customTopics));
        this.mqttTopics = topics.toArray(new String[0]);
    }

    @Override
    public String nodeId() {
        return nodeId;
    }

    @Override
    public void process(DriverHandler handler) {
        this.handler = Objects.requireNonNull(handler);
    }

    @Override
    public void publish(String mqttTopic, Message msg, int qos, boolean retained) {
        this.publishMQTT(mqttTopic, msg, qos, retained);
    }

    @Override
    public int nextMessageSequenceId() {
        return sequenceId.getAndSet((sequenceId.get() + 1) % Integer.MAX_VALUE);
    }

    @Override
    public Message nextMessageBy(String virtualId, byte[] body) {
        return Message.newMessageWith(this.nodeId, virtualId, body, this.nextMessageSequenceId());
    }

    @Override
    public Message nextMessageOf(String virtualNodeId, byte[] body) {
        return Message.newMessageById(virtualNodeId, body, this.nextMessageSequenceId());
    }

    @Override
    public void addStartupListener(OnStartupListener<Driver> l) {
        this.startupListeners.add(l);
    }

    @Override
    public void addShutdownListener(OnShutdownListener<Driver> l) {
        this.shutdownListeners.add(l);
    }

    @Override
    public void startup() {
        this.startupListeners.forEach(l -> l.onBefore(this));
        this.stats.up();

        // 监听所有Trigger的UserTopic
        final IMqttMessageListener listener = (rawMqttTopic, message) -> {
            final byte[] bytes = message.getPayload();
            final String topic = Topics.unwrapEdgeXTopic(rawMqttTopic);
            this.stats.updateRecv(bytes.length);
            try {
                handler.handle(topic, Message.parse(bytes));
            } catch (Exception e) {
                log.error("消息处理出错", e);
            }
        };

        final int qos = this.globals.getMqttQoS();
        try {
            for (String topic : this.mqttTopics) {
                log.debug("开启监听事件: QOS= " + qos + ", Topic= " + topic);
                this.mqttClientRef.subscribe(topic, qos, listener);
            }
        } catch (MqttException e) {
            log.fatal("监听TRIGGER事件出错：", e);
        }

        try {
            this.mqttClientRef.subscribe(
                    Topics.formatRepliesListen(this.nodeId),
                    router::dispatchThenRemove);
        } catch (MqttException e) {
            log.fatal("监听RPC事件出错：", e);
        }

        this.startupListeners.forEach(l -> l.onAfter(this));

        // 定时发送Stats
        this.statsTimer.scheduleAtFixedRate(new TimerTask() {

            private final String mqttTopic = Topics.formatEvents(nodeId);

            @Override
            public void run() {
                publishMQTT(mqttTopic,
                        nextMessageBy(nodeId, stats.toJSONString().getBytes()),
                        0,
                        false);
            }
        }, 3000, 1000 * 10);
    }

    @Override
    public void shutdown() {
        this.shutdownListeners.forEach(l -> l.onBefore(this));
        for (String t : this.mqttTopics) {
            log.debug("取消监听事件: " + t);
        }

        try {
            this.mqttClientRef.unsubscribe(this.mqttTopics);
        } catch (MqttException e) {
            log.fatal("取消监听出错：", e);
        }

        this.statsTimer.cancel();
        this.statsTimer.purge();
        this.shutdownListeners.forEach(l -> l.onAfter(this));
    }

    @Override
    public Message execute(String remoteNodeId, Message in, int timeoutSec) throws Exception {
        final CountDownLatch cdl = new CountDownLatch(1);
        final Wrap<Message> outW = new Wrap<>();
        this.call(remoteNodeId, in, out -> {
            outW.set(out);
            cdl.countDown();
        });
        cdl.await(timeoutSec, TimeUnit.SECONDS);
        return outW.get();
    }

    @Override
    public void call(String remoteNodeId, Message in, Callback cb) {
        log.debug("MQ_RPC调用Endpoint.NodeId: " + remoteNodeId);
        try {
            this.mqttClientRef.publish(
                    Topics.formatRequestSend(remoteNodeId, in.sequenceId(), this.nodeId),
                    in.bytes(),
                    this.globals.getMqttQoS(),
                    false
            );
        } catch (MqttException e) {
            log.error("发送MQ_RPC消息出错: ", e);
        }
        final String topic = Topics.formatRepliesFilter(remoteNodeId, in.sequenceId(), this.nodeId);
        this.router.register(topic, (t, msg) -> {
            cb.onMessage(Message.parse(msg.getPayload()));
        });
    }

    private void publishMQTT(String mqttTopic, Message msg, int qos, boolean retained) {
        final MqttClient mqtt = Objects.requireNonNull(this.mqttClientRef, "Mqtt客户端尚未启动");
        try {
            mqtt.publish(
                    mqttTopic,
                    msg.bytes(),
                    qos,
                    retained);
        } catch (MqttException e) {
            log.error("发送MQTT消息出错: " + mqttTopic, e);
        }
    }

    ////

    public static class Stats {

        private long uptime;
        private int recvCount;
        private long recvBytes;

        public void up() {
            this.uptime = System.currentTimeMillis();
        }

        public void updateRecv(long size) {
            this.recvCount++;
            this.recvBytes += size;
        }

        public String toJSONString() {
            final long du = (System.currentTimeMillis() - this.uptime) / 1000;
            return "{" +
                    "\"uptime\": " + du +
                    ", \"recvCount\":  " + this.recvCount +
                    ", \"recvBytes\":  " + this.recvBytes +
                    "}";
        }
    }

    ////

    private static class Wrap<T> {
        private T value;

        public T get() {
            return value;
        }

        public void set(T value) {
            this.value = value;
        }
    }

    ////

    private static class Router {

        private final Map<String, RouterHandler> handlers = new ConcurrentHashMap<>();

        void dispatchThenRemove(String topic, MqttMessage msg) {
            final RouterHandler cb = handlers.remove(topic);
            if (cb != null) {
                cb.onMessage(topic, msg);
            }
        }

        void register(String topic, RouterHandler callback) {
            handlers.put(topic, callback);
        }
    }

    ////

    private interface RouterHandler {
        void onMessage(String topic, MqttMessage msg);
    }

}
