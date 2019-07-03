package net.nextabc.edgex;

/**
 * @author 陈永佳 (yoojiachen@gmail.com)
 * @version 0.0.1
 */
public interface Driver extends LifeCycle {

    /**
     * 处理消息
     *
     * @param handler 处理函数
     */
    void process(MessageHandler handler);

    /**
     * 发送节点状态报告消息
     *
     * @param stat 状态消息
     */
    void publishStat(Message stat);

    /**
     * 发送MQTT消息
     *
     * @param topic   MQTT完整Topic
     * @param message 消息
     */
    void publish(String topic, Message message);

    /**
     * 发起一个消息请求，并获取响应消息。
     *
     * @param endpointAddress 　目标Endpoint的地址
     * @param in              　请求消息
     * @param timeoutSec      　请求超时时间
     * @return 响应消息
     * @throws Exception 如果过程中发生错误，返回错误消息
     */
    Message execute(String endpointAddress, Message in, int timeoutSec) throws Exception;

    ////

    final class Options {

        final int sendStatIntervalSec;
        final String nodeName;
        final String[] topics;

        public Options(String nodeName, String[] topics) {
            this(60, nodeName, topics);
        }

        public Options(int sendStatIntervalSec, String nodeName, String[] topics) {
            this.sendStatIntervalSec = sendStatIntervalSec;
            this.nodeName = nodeName;
            this.topics = topics;
        }
    }
}
