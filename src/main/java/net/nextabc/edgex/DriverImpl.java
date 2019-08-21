package net.nextabc.edgex;

import net.nextabc.edgex.internal.MessageRouter;
import net.nextabc.edgex.internal.SnowflakeId;
import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author 陈永佳 (yoojiachen@gmail.com)
 * @version 0.0.1
 */
final class DriverImpl implements Driver {

    private static final Logger log = Logger.getLogger(DriverImpl.class);

    private final Globals globals;

    private final Options options;
    private final Set<String> subscribedTopics = new HashSet<>();

    private final List<OnStartupListener<Driver>> startupListeners = new ArrayList<>(0);
    private final List<OnShutdownListener<Driver>> shutdownListeners = new ArrayList<>(0);
    private final MessageRouter router = new MessageRouter();

    private final MqttClient mqttClientRef;
    private final String nodeId;
    private final SnowflakeId eventIdRef;

    private final Statistics statistics = new Statistics();
    private final String statisticsMqttTopic;
    private final Timer statisticsTimer;

    private boolean stateStarted = false;
    private DriverHandler handler;

    DriverImpl(String nodeId, MqttClient mqttClient, SnowflakeId snowflakeId, Globals globals, Options options) {
        this.nodeId = nodeId;
        this.mqttClientRef = mqttClient;
        this.globals = Objects.requireNonNull(globals);
        this.eventIdRef = snowflakeId;

        this.statisticsTimer = new Timer("DriverStatisticsTimer");
        this.statisticsMqttTopic = Topics.formatStatistics(nodeId);
        this.options = options;
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
    public void publishMqtt(String mqttTopic, Message msg, int qos, boolean retained) throws MqttException {
        this.mqttPublishMessage(mqttTopic, msg, qos, retained);
    }

    @Override
    public void publishEvent(String topic, Message message) throws MqttException {
        this.mqttPublishMessage(Topics.formatEvents(topic),
                message,
                this.globals.getMqttQoS(),
                this.globals.isMqttRetained());
    }

    @Override
    public void publishValue(String topic, Message message) throws MqttException {
        this.mqttPublishMessage(Topics.formatValues(topic),
                message,
                this.globals.getMqttQoS(),
                this.globals.isMqttRetained());
    }

    @Override
    public void publishStatistics(byte[] data) throws MqttException {
        mqttPublishMessage(
                statisticsMqttTopic,
                Message.newMessage(
                        this.nodeId, this.nodeId, this.nodeId, null,
                        data, generateEventId()),
                0,
                false);
    }

    @Override
    public long generateEventId() {
        return eventIdRef.nextId();
    }

    @Override
    public Message newMessage(String groupId, String majorId, String minorId, byte[] body, long eventId) {
        return Message.newMessage(this.nodeId, groupId, majorId, minorId, body, eventId);
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
        this.stateStarted = true;
        this.startupListeners.forEach(l -> l.onBefore(this));
        this.statistics.up();

        final int qos = this.globals.getMqttQoS();
        for (Map.Entry<TopicType, Set<String>> item : this.options.topicMapping.entrySet()) {
            final TopicType type = item.getKey();
            final IMqttMessageListener listener = (rawMqttTopic, message) -> {
                final byte[] bytes = message.getPayload();
                this.statistics.updateRecv(bytes.length);
                final String exTopic = Topics.unwrapEdgeXTopic(rawMqttTopic);
                try {
                    handler.handle(type, exTopic, Message.parse(bytes));
                } catch (Exception e) {
                    log.error("消息处理出错", e);
                }
            };
            for (String topic : item.getValue()) {
                final String mqttTopic;
                switch (type) {
                    default:
                    case Custom:
                        mqttTopic = topic;
                        break;
                    case Events:
                        mqttTopic = Topics.formatEvents(topic);
                        break;
                    case Values:
                        mqttTopic = Topics.formatValues(topic);
                        break;
                    case Actions:
                        mqttTopic = Topics.formatActions(topic);
                        break;
                    case Properties:
                        mqttTopic = Topics.formatProperties(topic);
                        break;
                    case Statistics:
                        mqttTopic = Topics.formatStatistics(topic);
                        break;
                }
                log.debug("开启订阅事件: QOS= " + qos + ", Topic= " + mqttTopic);
                this.subscribedTopics.add(mqttTopic);
                try {
                    this.mqttClientRef.subscribe(mqttTopic, qos, listener);
                } catch (MqttException e) {
                    log.error("订阅事件出错", e);
                }
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
        this.statisticsTimer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                try {
                    publishStatistics(statistics.toJSONString().getBytes());
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
        for (String t : this.subscribedTopics) {
            log.debug("取消监听事件: " + t);
        }
        try {
            this.mqttClientRef.unsubscribe(this.subscribedTopics.toArray(new String[0]));
        } catch (MqttException e) {
            log.fatal("取消监听出错：", e);
        }
        this.statisticsTimer.cancel();
        this.statisticsTimer.purge();
        this.shutdownListeners.forEach(l -> l.onAfter(this));
        this.stateStarted = false;
    }

    @Override
    public Message execute(String nodeId, String boardId, String majorId, String minorId,
                           byte[] body, long eventId, int timeoutSec) throws Exception {
        return call(nodeId, boardId, majorId, minorId, body, eventId).get(timeoutSec, TimeUnit.SECONDS);
    }

    @Override
    public Message execute(Message message, int timeoutSec) throws Exception {
        return call(message).get(timeoutSec, TimeUnit.SECONDS);
    }

    @Override
    public CompletableFuture<Message> call(Message message) {
        checkState();
        final long eventId = message.eventId();
        final String executorNodeId = message.nodeId();
        final String callerNodeId = this.nodeId;
        if (globals.isLogVerbose()) {
            log.debug("MQ_RPC调用，RemoteExecutorNodeId: " + executorNodeId + ", EventId: " + eventId);
        }
        try {
            mqttPublishMessage(
                    Topics.formatRequestSend(executorNodeId, callerNodeId),
                    message,
                    this.globals.getMqttQoS(),
                    false
            );
        } catch (MqttException e) {
            log.error("MQ_RPC调用，发送MQTT消息出错", e);
            return CompletableFuture.completedFuture(
                    Message.newMessageByUnionId(
                            message.unionId(),
                            ("MQ_RPC_MQTT_ERR:" + e.getMessage()).getBytes(),
                            eventId
                    ));
        }
        final String topic = Topics.formatRepliesFilter(executorNodeId, callerNodeId);
        return this.router.register(topic, msg -> eventId == msg.eventId());
    }

    @Override
    public CompletableFuture<Message> call(String nodeId, String boardId, String majorId, String minorId, byte[] body, long eventId) {
        return call(Message.newMessage(nodeId, boardId, majorId, minorId, body, eventId));
    }

    private void mqttPublishMessage(String mqttTopic, Message msg, int qos, boolean retained) throws MqttException {
        checkState();
        final MqttClient mqtt = Objects.requireNonNull(this.mqttClientRef, "Mqtt客户端尚未启动");
        mqtt.publish(
                mqttTopic,
                msg.bytes(),
                qos,
                retained);
    }

    private void checkState() {
        if (!this.stateStarted) {
            log.fatal("Driver未启动，须调用startup()/shutdown()");
        }
    }

    ////

    public static class Statistics {

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
