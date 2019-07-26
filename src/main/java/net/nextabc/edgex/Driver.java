package net.nextabc.edgex;

/**
 * @author 陈永佳 (yoojiachen@gmail.com)
 * @version 0.0.1
 */
public interface Driver extends LifeCycle {

    /**
     * 返回节点名称
     *
     * @return 节点名称
     */
    String nodeName();

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

    /**
     * Hello 发起一个同步Hello消息，并获取响应消息。通常使用此函数来触发gRPC创建并预热两个节点之间的连接。
     *
     * @param endpointAddress 目标Endpoint的地址
     * @param timeoutSec      请求超时时间
     * @return 响应消息，消息体内容为 WORLD
     * @throws Exception 如果过程中发生错误，返回错误消息
     */
    Message hello(String endpointAddress, int timeoutSec) throws Exception;

    /**
     * 返回消息流水号
     *
     * @return 消息流水号
     */
    int nextSequenceId();

    /**
     * 基于内部消息流水号，创建消息对象
     *
     * @param virtualNodeId 虚拟虚名ID
     * @param body          Body
     * @return 消息对象
     */
    Message nextMessage(String virtualNodeId, byte[] body);

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

        final int sendStatIntervalSec;
        final String[] topics;

        public Options(String[] topics) {
            this(60, topics);
        }

        public Options(int sendStatIntervalSec, String[] topics) {
            this.sendStatIntervalSec = sendStatIntervalSec;
            this.topics = topics;
        }
    }
}
