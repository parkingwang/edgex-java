package net.nextabc.edgex;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author 陈哈哈 (yoojiachen@gmail.com)
 */
public interface Driver extends LifeCycle, NodeId, Messaging {

    /**
     * 处理消息
     *
     * @param handler 处理函数
     */
    void process(DriverHandler handler);

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
     *
     * @param remoteNodeId        　目标Endpoint的地址
     * @param remoteVirtualNodeId 　目标Virtual Node id
     * @param body                　Body
     * @param seqId               指定流水ID
     * @param timeoutSec          　请求超时时间
     * @return 响应消息
     * @throws Exception 如果过程中发生错误，返回错误消息
     */
    Message executeById(String remoteNodeId, String remoteVirtualNodeId, byte[] body, long seqId, int timeoutSec) throws Exception;

    /**
     * 发起一个同步调用消息请求，并获取响应消息。使用内部流水ID。
     *
     * @param remoteNodeId        　目标Endpoint的地址
     * @param remoteVirtualNodeId 　目标Virtual Node id
     * @param body                　Body
     * @param timeoutSec          　请求超时时间
     * @return 响应消息
     * @throws Exception 如果过程中发生错误，返回错误消息
     */
    Message executeNextId(String remoteNodeId, String remoteVirtualNodeId, byte[] body, int timeoutSec) throws Exception;

    /**
     * 发起一个异步请求，返回CompletableFuture对象
     *
     * @param remoteNodeId        　目标Endpoint的地址
     * @param remoteVirtualNodeId 　目标Virtual Node id
     * @param body                　Body
     * @return CompletableFuture
     */
    CompletableFuture<Message> call(String remoteNodeId, String remoteVirtualNodeId, byte[] body, long seqId);

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

        final List<String> eventTopics;
        final List<String> valueTopics;
        final List<String> customTopics;

        public Options() {
            this(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        }

        public Options(List<String> eventTopics, List<String> valueTopics, List<String> customTopics) {
            this.eventTopics = eventTopics == null ? Collections.emptyList() : eventTopics;
            this.valueTopics = valueTopics == null ? Collections.emptyList() : valueTopics;
            this.customTopics = customTopics == null ? Collections.emptyList() : customTopics;
        }

        public Options addEventTopic(String... topics) {
            eventTopics.addAll(Arrays.asList(topics));
            return this;
        }

        public Options addValueTopic(String... topics) {
            valueTopics.addAll(Arrays.asList(topics));
            return this;
        }

        public Options addCustomTopic(String... topics) {
            customTopics.addAll(Arrays.asList(topics));
            return this;
        }
    }
}
