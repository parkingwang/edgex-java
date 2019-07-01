package net.nextabc.edgex;

import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.Objects;

/**
 * @author 陈永佳 (yoojiachen@gmail.com)
 * @version 0.0.1
 */
final class DriverImpl implements Driver {

    private static final Logger log = Logger.getLogger(DriverImpl.class);

    private MessageHandler handler;
    private final Globals globals;
    private final Driver.Options options;

    private final Executor executor;

    private final String[] mqttTopics;
    private MqttClient mqttClient;

    DriverImpl(Globals globals, Options options) {
        this.globals = Objects.requireNonNull(globals);
        this.options = Objects.requireNonNull(options);
        this.executor = new ExecutorImpl(this.globals);
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
    public Message execute(String endpointAddress, Message in, int timeoutSec) throws Exception {
        return this.executor.execute(endpointAddress, in, timeoutSec);
    }

    @Override
    public void startup() {
        final String clientId = "EX-Driver-" + this.options.nodeName;
        final MemoryPersistence mp = new MemoryPersistence();
        try {
            this.mqttClient = new MqttClient(this.globals.mqttBroker, clientId, mp);
        } catch (MqttException e) {
            log.fatal("Mqtt客户端错误", e);
        }
        final MqttConnectOptions opts = new MqttConnectOptions();
        opts.setWill(Topics.topicOfOffline("Driver", this.options.nodeName), "offline".getBytes(), 1, true);
        Mqtt.setup(this.globals, opts);
        for (int i = 0; i < globals.mqttMaxRetry; i++) {
            try {
                this.mqttClient.connect(opts);
                log.info("Mqtt客户端连接成功");
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
        }

        // 监听所有Trigger的UserTopic
        final IMqttMessageListener listener = (topic, message) -> {
            try {
                handler.handle(Message.parse(message.getPayload()));
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
            log.fatal("Mqtt客户端出错：", e);
        }
    }

    @Override
    public void shutdown() {

        for (String t : this.mqttTopics) {
            log.debug("取消监听事件[TRIGGER]: " + t);
        }

        try {
            this.mqttClient.unsubscribe(this.mqttTopics);
        } catch (MqttException e) {
            log.fatal("Mqtt客户端出错：", e);
        }
    }
}
