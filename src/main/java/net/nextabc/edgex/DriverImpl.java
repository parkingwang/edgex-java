package net.nextabc.edgex;

import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 陈永佳 (yoojiachen@gmail.com)
 * @version 0.0.1
 */
final class DriverImpl implements Driver {

    private static final Logger log = Logger.getLogger(DriverImpl.class);

    private final Globals globals;
    private final Driver.Options options;

    private final Executor executor;
    private final Timer statTimer;

    private final String[] mqttTopics;
    private final Stats stats = new Stats();
    private final AtomicInteger sequenceId = new AtomicInteger(0);
    private final List<OnStartupListener<Driver>> startupListeners = new ArrayList<>(0);
    private final List<OnShutdownListener<Driver>> shutdownListeners = new ArrayList<>(0);

    private final MqttClient mqttClientRef;
    private final String nodeName;
    private DriverHandler handler;

    DriverImpl(String nodeName, MqttClient mqttClient, Globals globals, Options options) {
        this.nodeName = nodeName;
        this.mqttClientRef = mqttClient;
        this.globals = Objects.requireNonNull(globals);
        this.options = Objects.requireNonNull(options);
        this.executor = new ExecutorImpl(this.globals);
        this.statTimer = new Timer("DriverStatTimer");
        // topics
        final int size = this.options.topics.length;
        if (size == 0) {
            log.fatal("Mqtt topics must be specified");
        }
        this.mqttTopics = new String[size];
        for (int i = 0; i < size; i++) {
            final String topic = this.options.topics[i];
            if (Topics.isTopLevelTopic(topic)) {
                this.mqttTopics[i] = topic;
            } else {
                this.mqttTopics[i] = Topics.wrapTriggerEvents(topic);
            }
        }
    }

    @Override
    public String nodeName() {
        return nodeName;
    }

    @Override
    public void process(DriverHandler handler) {
        this.handler = Objects.requireNonNull(handler);
    }

    @Override
    public void publishStat(Message stat) {
        // Stat消息参数：QoS 0，not retained
        this.publishMQTT(Topics.wrapStat(this.nodeName), stat, 0, false);
    }

    @Override
    public void publish(String mqttTopic, Message msg) {
        this.publishMQTT(mqttTopic, msg, this.globals.getMqttQoS(), this.globals.isMqttRetained());
    }

    @Override
    public Message execute(String endpointAddress, Message in, int timeoutSec) throws Exception {
        return this.executor.execute(endpointAddress, in, timeoutSec);
    }

    @Override
    public Message hello(String endpointAddress, int timeoutSec) throws Exception {
        return this.executor.execute(
                endpointAddress,
                Message.create(Message.makeSourceNodeId(this.nodeName, this.nodeName),
                        new byte[0], Message.FrameVarPing, nextSequenceId()),
                timeoutSec);
    }

    @Override
    public int nextSequenceId() {
        return sequenceId.getAndSet((sequenceId.get() + 1) % Integer.MAX_VALUE);
    }

    @Override
    public Message nextMessage(String virtualNodeId, byte[] body) {
        return Message.fromBytes(this.nodeName, virtualNodeId, body, nextSequenceId());
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
        final IMqttMessageListener listener = (mqttTopic, message) -> {
            final byte[] bytes = message.getPayload();
            final String exTopic = Topics.unwrapTriggerEvents(mqttTopic);
            this.stats.updateRecv(bytes.length);
            try {
                handler.handle(exTopic, Message.parse(bytes));
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
            log.fatal("Mqtt客户端订阅事件出错：", e);
        }

        // Send stat message
        this.statTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                DriverImpl.this.publishStat(nextMessage(nodeName, stats.toJSONString().getBytes()));
            }
        }, 1000, this.options.sendStatIntervalSec * 1000);

        this.startupListeners.forEach(l -> l.onAfter(this));
    }

    @Override
    public void shutdown() {
        this.shutdownListeners.forEach(l -> l.onBefore(this));
        for (String t : this.mqttTopics) {
            log.debug("取消监听事件[TRIGGER]: " + t);
        }

        try {
            this.mqttClientRef.unsubscribe(this.mqttTopics);
        } catch (MqttException e) {
            log.fatal("Mqtt客户端出错：", e);
        }

        this.statTimer.cancel();
        this.statTimer.purge();
        this.shutdownListeners.forEach(l -> l.onAfter(this));
    }

    private void publishMQTT(String mqttTopic, Message msg, int qos, boolean retained) {
        final MqttClient cli = Objects.requireNonNull(this.mqttClientRef, "Mqtt客户端尚未启动");
        try {
            cli.publish(mqttTopic,
                    msg.bytes(),
                    qos,
                    retained);
        } catch (MqttException e) {
            log.error("发送MQTT消息出错", e);
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
}
