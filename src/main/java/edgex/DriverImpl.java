package edgex;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.net.URI;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author 陈永佳 (yoojiachen@gmail.com)
 * @version 0.0.1
 */
final class DriverImpl implements Driver {

    private static final Logger log = Logger.getLogger(DriverImpl.class);

    private Consumer<Message> func;
    private final GlobalScoped scoped;
    private final Driver.Options options;

    private final String[] mqttTopics;
    private MqttClient mqttClient;

    DriverImpl(GlobalScoped scoped, Options options) {
        this.scoped = Objects.requireNonNull(scoped);
        this.options = Objects.requireNonNull(options);
        // topics
        final int size = this.options.topics.length;
        this.mqttTopics = new String[size];
        for (int i = 0; i < size; i++) {
            this.mqttTopics[i] = Topics.topicOfTrigger(this.options.topics[i]);
        }
    }

    @Override
    public void process(Consumer<Message> func) {
        this.func = Objects.requireNonNull(func);
    }

    @Override
    public Message execute(String endpointAddress, Message in, int timeoutSec) throws Exception {
        log.debug("GRPC调用Endpoint: " + endpointAddress);
        final URI uri = URI.create(endpointAddress);
        final ManagedChannel ch = ManagedChannelBuilder.forAddress(uri.getHost(), uri.getPort())
                .usePlaintext()
                .build();
        final ExecuteGrpc.ExecuteFutureStub stub = ExecuteGrpc.newFutureStub(ch);
        final Data req = Data.parseFrom(in.bytes());
        final byte[] rep = stub.execute(req).get(timeoutSec, TimeUnit.SECONDS).toByteArray();
        return Message.newBytes(rep);
    }

    @Override
    public void startup() {
        try {
            this.mqttClient = new MqttClient(this.scoped.mqttBroker,
                    "Driver-" + this.options.name,
                    new MemoryPersistence());
            MqttConnectOptions opts = new MqttConnectOptions();
            opts.setCleanSession(this.scoped.mqttClearSession);
            opts.setAutomaticReconnect(this.scoped.mqttAutoReconnect);
            opts.setKeepAliveInterval(this.scoped.mqttKeepAlive);
            opts.setConnectionTimeout(this.scoped.mqttConnectTimeout);
            log.info("Mqtt客户端连接Broker: " + this.scoped.mqttBroker);
            this.mqttClient.connect(opts);
            log.info("Mqtt客户端连接成功");
        } catch (MqttException e) {
            log.fatal("Mqtt客户端出错：", e);
            return;
        }
        // 监听所有Trigger的UserTopic
        for (String t : this.mqttTopics) {
            log.debug("开启监听事件[TRIGGER]: " + t);
        }
        try {
            this.mqttClient.subscribe(this.mqttTopics, new IMqttMessageListener[]{
                    (topic, message) -> this.func.accept(Message.newBytes(message.getPayload()))
            });
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
