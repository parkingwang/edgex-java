package net.nextabc.edgex;

import net.nextabc.edgex.internal.MessageRouter;
import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.*;
import java.util.concurrent.CompletableFuture;
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
    private final MessageRouter router = new MessageRouter();

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
        topics.addAll(options.customTopics);
        this.mqttTopics = topics.toArray(new String[0]);
        // 一些场景中，Driver只用作驱动，不监听任何事件
        if (this.mqttTopics.length == 0) {
            log.warn("Driver未监听任何事件：" + nodeId);
        }
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
    public void publishMqtt(String mqttTopic, Message msg, int qos, boolean retained) {
        this.tryPublishMqtt(mqttTopic, msg, qos, retained);
    }

    @Override
    public void publishEvents(String topic, Message message) {
        this.tryPublishMqtt(Topics.formatEvents(topic),
                message,
                this.globals.getMqttQoS(),
                this.globals.isMqttRetained());
    }

    @Override
    public void publishValues(String topic, Message message) {
        this.tryPublishMqtt(Topics.formatValues(topic),
                message,
                this.globals.getMqttQoS(),
                this.globals.isMqttRetained());
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

        if (this.mqttTopics.length > 0) {
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
        }

        // Async RPC监听
        try {
            this.mqttClientRef.subscribe(
                    Topics.formatRepliesListen(this.nodeId),
                    router::route);
        } catch (MqttException e) {
            log.fatal("监听RPC事件出错：", e);
        }

        // 定时发送Stats
        this.statsTimer.scheduleAtFixedRate(new TimerTask() {

            private final String mqttTopic = Topics.formatStats(nodeId);

            @Override
            public void run() {
                try {
                    mqttPublishMessage(
                            mqttTopic,
                            nextMessageBy(nodeId, stats.toJSONString().getBytes()),
                            0,
                            false);
                } catch (MqttException e) {
                    log.debug("定时上报Stats消息，MQTT发送错误：", e);
                }
            }
        }, 3000, 1000 * 10);

        this.startupListeners.forEach(l -> l.onAfter(this));
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
    public Message execute(String remoteNodeId, String remoteVirtualNodeId, byte[] body, int timeoutSec) throws Exception {
        return call(remoteNodeId, remoteVirtualNodeId, body).get(timeoutSec, TimeUnit.SECONDS);
    }

    @Override
    public CompletableFuture<Message> call(String remoteNodeId, String remoteVirtualNodeId, byte[] body) {
        final Message req = nextMessageOf(remoteVirtualNodeId, body);
        final int seqId = req.sequenceId();

        log.debug("MQ_RPC调用，RemoteNodeId: " + remoteNodeId + ", SeqId: " + seqId);
        try {
            mqttPublishMessage(
                    Topics.formatRequestSend(remoteNodeId, this.nodeId),
                    req,
                    this.globals.getMqttQoS(),
                    false
            );
        } catch (MqttException e) {
            log.error("MQ_RPC调用，发送MQTT消息出错", e);
            return CompletableFuture.completedFuture(
                    nextMessageBy("ERROR", ("MQ_RPC_MQTT_ERR:" + e.getMessage()).getBytes()));
        }
        final String topic = Topics.formatRepliesFilter(remoteNodeId, this.nodeId);
        return this.router.register(topic, msg -> seqId == msg.sequenceId());
    }

    private void tryPublishMqtt(String mqttTopic, Message msg, int qos, boolean retained) {
        for (int i = 0; i < 5; i++) {
            try {
                mqttPublishMessage(mqttTopic, msg, qos, retained);
                return;
            } catch (MqttException e) {
                log.error(String.format("[%d]发送MQTT消息失败，重试发送", i), e);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    log.error("重试发送MQTT消息被中断");
                    return;
                }
            }
        }
    }

    private void mqttPublishMessage(String mqttTopic, Message msg, int qos, boolean retained) throws MqttException {
        final MqttClient mqtt = Objects.requireNonNull(this.mqttClientRef, "Mqtt客户端尚未启动");
        mqtt.publish(
                mqttTopic,
                msg.bytes(),
                qos,
                retained);
    }

    ////

    public static class Stats {

        private long uptime;
        private int recvCount;
        private long recvBytes;

        void up() {
            this.uptime = System.currentTimeMillis();
        }

        void updateRecv(long size) {
            this.recvCount++;
            this.recvBytes += size;
        }

        String toJSONString() {
            final long du = (System.currentTimeMillis() - this.uptime) / 1000;
            return "{" +
                    "\"uptime\": " + du +
                    ", \"recvCount\":  " + this.recvCount +
                    ", \"recvBytes\":  " + this.recvBytes +
                    "}";
        }
    }

}
