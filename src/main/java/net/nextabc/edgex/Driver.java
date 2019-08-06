package net.nextabc.edgex;

import lombok.Builder;

/**
 * @author 陈永佳 (yoojiachen@gmail.com)
 * @version 0.0.1
 */
public interface Driver extends LifeCycle, NeedNodeId, NeedMessages {

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
     */
    void publish(String mqttTopic, Message message, int qos, boolean retained);

    /**
     * 发起一个消息请求，并获取响应消息。
     *
     * @param remoteNodeId 　目标Endpoint的地址
     * @param in           　请求消息
     * @param timeoutSec   　请求超时时间
     * @return 响应消息
     * @throws Exception 如果过程中发生错误，返回错误消息
     */
    Message execute(String remoteNodeId, Message in, int timeoutSec) throws Exception;

    /**
     * 发起一个异步请求
     *
     * @param remoteNodeId 远程节点ID
     * @param in           Message
     * @param cb           回调接口
     */
    void call(String remoteNodeId, Message in, Callback cb);

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

    @FunctionalInterface
    interface Callback {
        void onMessage(Message out);
    }

    ////

    @Builder
    final class Options {

        final int sendStatIntervalSec;
        final String[] eventTopics;
        final String[] valueTopics;
        final String[] customTopics;

        public Options(String[] eventTopics, String[] valueTopics, String[] customTopics) {
            this(60, eventTopics, valueTopics, customTopics);
        }

        public Options(int sendStatIntervalSec, String[] eventTopics, String[] valueTopics, String[] customTopics) {
            this.sendStatIntervalSec = sendStatIntervalSec;
            this.eventTopics = eventTopics;
            this.valueTopics = valueTopics;
            this.customTopics = customTopics;
        }
    }
}
