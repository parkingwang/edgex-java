package net.nextabc.edgex;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author 陈永佳 (yoojiachen@gmail.com)
 * @version 0.0.1
 */
final class DriverImpl implements Driver {

    private static final Logger log = Logger.getLogger(DriverImpl.class);

    private MessageHandler handler;
    private final GlobalScoped scoped;
    private final Driver.Options options;
    private final ExpiringMap<String, ManagedChannel> channels = new ExpiringMap<>(ManagedChannel::shutdown);

    private final String[] mqttTopics;
    private MqttClient mqttClient;

    DriverImpl(GlobalScoped scoped, Options options) {
        this.scoped = Objects.requireNonNull(scoped);
        this.options = Objects.requireNonNull(options);
        // topics
        final int size = this.options.topics.length;
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
        log.debug("GRPC调用Endpoint: " + endpointAddress);
        ManagedChannel channel;
        synchronized (channels) {
            channel = channels.get(endpointAddress);
            if (null == channel) {
                channel = ManagedChannelBuilder.forTarget(endpointAddress)
                        .keepAliveWithoutCalls(true)
                        .keepAliveTimeout(30, TimeUnit.SECONDS)
                        .usePlaintext()
                        .build();
                // keep 6 hours
                channels.put(endpointAddress, channel, 1000 * 60 * 60 * 6);
            }
        }
        final ExecuteGrpc.ExecuteFutureStub stub = ExecuteGrpc.newFutureStub(channel);
        final Data req = Data.newBuilder()
                .setFrames(ByteString.copyFrom(in.getFrames()))
                .build();
        return Message.parse(stub
                .execute(req)
                .get(timeoutSec, TimeUnit.SECONDS)
                .getFrames()
                .toByteArray());
    }

    @Override
    public void startup() {
        for (int i = 0; i < scoped.mqttMaxRetry; i++) {
            try {
                this.mqttClient = new MqttClient(this.scoped.mqttBroker,
                        "Driver-" + this.options.nodeName,
                        new MemoryPersistence());
                MqttConnectOptions opts = new MqttConnectOptions();
                opts.setCleanSession(this.scoped.mqttClearSession);
                opts.setAutomaticReconnect(this.scoped.mqttAutoReconnect);
                opts.setKeepAliveInterval(this.scoped.mqttKeepAlive);
                opts.setConnectionTimeout(this.scoped.mqttConnectTimeout);
                log.info("Mqtt客户端连接Broker: " + this.scoped.mqttBroker);
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
                log.debug("开启监听事件: " + topic);
                this.mqttClient.subscribe(topic, 0, listener);
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
