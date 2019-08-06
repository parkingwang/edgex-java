package net.nextabc.edgex;

/**
 * @author 陈哈哈 (yoojiachen@gmail.com)
 */
public interface Publishable {

    /**
     * 发送MQTT消息
     *
     * @param mqttTopic MQTT完整Topic
     * @param message   消息
     */
    void publishMqtt(String mqttTopic, Message message, int qos, boolean retained);

    /**
     * 发送Events消息
     *
     * @param topic   Topic
     * @param message Message数据
     */
    void publishEvents(String topic, Message message);

    /**
     * 发送Values消息
     *
     * @param topic   Topic
     * @param message Message数据
     */
    void publishValues(String topic, Message message);
}
