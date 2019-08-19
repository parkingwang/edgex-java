package net.nextabc.edgex;

/**
 * @author 陈永佳 (yoojiachen@gmail.com)
 */
@FunctionalInterface
public interface DriverHandler {

    /**
     * 处理消息
     *
     * @param type    事件类型
     * @param topic   EdgeXTopic
     * @param message 　接收到的消息
     * @throws Exception 　发生异常
     */
    void handle(TopicType type, String topic, Message message) throws Exception;
}
