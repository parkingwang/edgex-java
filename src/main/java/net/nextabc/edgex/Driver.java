package net.nextabc.edgex;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * @author 陈哈哈 (yoojiachen@gmail.com)
 */
public interface Driver extends LifeCycle, NodeId {

    /**
     * 处理消息
     *
     * @param handler 处理函数
     */
    void process(DriverHandler handler);

    /**
     * 返回内部消息流水号
     *
     * @return 消息流水号
     */
    long generateEventId();

    /**
     * 创建本节点消息对象
     *
     * @param groupId 虚拟ID
     * @param body    Body
     * @param eventId 事件ID
     * @return 消息对象
     */
    Message newMessage(String groupId, String majorId, String minorId, byte[] body, long eventId);

    /**
     * 发送MQTT消息
     *
     * @param mqttTopic MQTT完整Topic
     * @param message   消息
     * @param qos       Mqtt Qos
     * @param retained  Mqtt retained
     * @throws MqttException Mqtt发送错误
     */
    void publishMqtt(String mqttTopic, Message message, int qos, boolean retained) throws MqttException;

    /**
     * 发送Events消息
     *
     * @param topic   Topic
     * @param message Message数据
     * @throws MqttException Mqtt发送错误
     */
    void publishEvent(String topic, Message message) throws MqttException;

    /**
     * 发送Values消息
     *
     * @param topic   Topic
     * @param message Message数据
     * @throws MqttException Mqtt发送错误
     */
    void publishValue(String topic, Message message) throws MqttException;

    /**
     * 发送Statistics消息
     *
     * @param data Statistics数据
     * @throws MqttException Mqtt发送错误
     */
    void publishStatistics(byte[] data) throws MqttException;

    /**
     * 发起一个同步调用消息请求，并获取响应消息。
     */
    Message execute(String nodeId, String groupId, String majorId, String minorId, byte[] body,
                    long eventId, int timeoutSec) throws Exception;

    /**
     * 发起一个同步调用消息请求，并获取响应消息。
     */
    Message execute(Message message, int timeoutSec) throws Exception;

    /**
     * 发起一个异步请求，返回CompletableFuture对象
     */
    CompletableFuture<Message> call(String nodeId, String groupId, String majorId, String minorId, byte[] body, long eventId);

    /**
     * 发起一个异步请求，返回CompletableFuture对象
     */
    CompletableFuture<Message> call(Message message);

    /**
     * 添加Startup监听接口
     *
     * @param l 监听接口
     */
    void addStartupListener(OnStartupListener<Driver> l);

    /**
     * 添加Shutdown监听接口
     *
     * @param l 监听接口
     */
    void addShutdownListener(OnShutdownListener<Driver> l);

    ////

    final class Options {

        final Map<TopicType, Set<String>> topicMapping;

        public Options() {
            topicMapping = new HashMap<>(TopicType.values().length);
            for (TopicType t : TopicType.values()) {
                topicMapping.put(t, new HashSet<>());
            }
        }

        public Options addEventsTopic(String... topics) {
            return addTopic(TopicType.Events, topics);
        }

        public Options addValuesTopic(String... topics) {
            return addTopic(TopicType.Values, topics);
        }

        public Options addActionTopic(String... topics) {
            return addTopic(TopicType.Actions, topics);
        }

        public Options addStatisticsTopic(String... topics) {
            return addTopic(TopicType.Statistics, topics);
        }

        public Options addPropertiesTopic(String... topics) {
            return addTopic(TopicType.Properties, topics);
        }

        public Options addCustomTopic(String... topics) {
            return addTopic(TopicType.Custom, topics);
        }

        public Options addTopic(TopicType type, String... topics) {
            final Set<String> t = topicMapping.get(type);
            t.addAll(Arrays.asList(topics));
            return this;
        }
    }
}
