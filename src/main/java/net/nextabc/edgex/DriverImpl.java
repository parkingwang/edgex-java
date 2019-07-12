package net.nextabc.edgex;

import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

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
    private final List<OnStartupListener> startupListeners = new ArrayList<>(0);
    private final List<OnShutdownListener> shutdownListeners = new ArrayList<>(0);

    private MqttClient mqttClient;
    private MessageHandler handler;

    DriverImpl(Globals globals, Options options) {
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
                this.mqttTopics[i] = Topics.topicOfTrigger(topic);
            }
        }
    }

    @Override
    public void process(MessageHandler handler) {
        this.handler = Objects.requireNonNull(handler);
    }

    @Override
    public void publishStat(Message stat) {
        // Stat消息参数：QoS 0，not retained
        this.publishMQTT(Topics.topicOfStat(this.options.nodeName), stat, 0, false);
    }

    @Override
    public void publish(String mqttTopic, Message msg) {
        this.publishMQTT(mqttTopic, msg, this.globals.mqttQoS, this.globals.mqttRetained);
    }

    @Override
    public Message execute(String endpointAddress, Message in, int timeoutSec) throws Exception {
        return this.executor.execute(endpointAddress, in, timeoutSec);
    }

    @Override
    public Message hello(String endpointAddress, int timeoutSec) throws Exception {
        return this.executor.execute(
                endpointAddress,
                Message.create(this.options.nodeName, new byte[0], Message.FrameVarPing, nextSequenceId()),
                timeoutSec);
    }

    @Override
    public int nextSequenceId() {
        return sequenceId.getAndSet((sequenceId.get() + 1) % Integer.MAX_VALUE);
    }

    @Override
    public Message nextMessage(String sourceName, byte[] body) {
        return Message.fromBytes(sourceName, body, nextSequenceId());
    }

    @Override
    public void addStartupListener(OnStartupListener l) {
        this.startupListeners.add(l);
    }

    @Override
    public void addShutdownListener(OnShutdownListener l) {
        this.shutdownListeners.add(l);
    }

    @Override
    public void startup() {
        this.startupListeners.forEach(OnStartupListener::onBefore);
        this.stats.up();
        final String clientId = "EX-Driver-" + this.options.nodeName;
        final MemoryPersistence mp = new MemoryPersistence();
        try {
            this.mqttClient = new MqttClient(this.globals.mqttBroker, clientId, mp);
        } catch (MqttException e) {
            log.fatal("Mqtt客户端错误", e);
        }
        final MqttConnectOptions opts = new MqttConnectOptions();
        opts.setWill(Topics.topicOfOffline("Driver", this.options.nodeName), "offline".getBytes(), 1, false);
        Mqtt.setup(this.globals, opts);
        for (int i = 0; i < globals.mqttMaxRetry; i++) {
            try {
                this.mqttClient.connect(opts);
                break;
            } catch (MqttException e) {
                log.error("Mqtt客户端出错：", e);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    break;
                }
            }
        }

        if (!this.mqttClient.isConnected()) {
            log.fatal("Mqtt客户端无法连接Broker");
        } else {
            log.info("Mqtt客户端连接成功: " + clientId);
        }

        // 监听所有Trigger的UserTopic
        final IMqttMessageListener listener = (topic, message) -> {
            try {
                final byte[] bytes = message.getPayload();
                this.stats.updateRecv(bytes.length);
                handler.handle(Message.parse(bytes));
            } catch (Exception e) {
                log.error("消息处理出错", e);
            }
        };
        try {
            for (String topic : this.mqttTopics) {
                log.debug("开启监听事件: QOS= " + this.globals.mqttQoS + ", Topic= " + topic);
                this.mqttClient.subscribe(topic, this.globals.mqttQoS, listener);
            }
        } catch (MqttException e) {
            log.fatal("Mqtt客户端订阅事件出错：", e);
        }

        // Send stat message
        this.statTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                DriverImpl.this.publishStat(nextMessage(options.nodeName, stats.toJSONString().getBytes()));
            }
        }, 1000, this.options.sendStatIntervalSec * 1000);

        this.startupListeners.forEach(OnStartupListener::onAfter);
    }

    @Override
    public void shutdown() {
        this.shutdownListeners.forEach(OnShutdownListener::onBefore);
        for (String t : this.mqttTopics) {
            log.debug("取消监听事件[TRIGGER]: " + t);
        }

        try {
            this.mqttClient.unsubscribe(this.mqttTopics);
        } catch (MqttException e) {
            log.fatal("Mqtt客户端出错：", e);
        }

        this.statTimer.cancel();
        this.statTimer.purge();
        this.shutdownListeners.forEach(OnShutdownListener::onAfter);
    }

    private void publishMQTT(String mqttTopic, Message msg, int qos, boolean retained) {
        final MqttClient cli = Objects.requireNonNull(this.mqttClient, "Mqtt客户端尚未启动");
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
