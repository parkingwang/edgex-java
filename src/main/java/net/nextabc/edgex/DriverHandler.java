package net.nextabc.edgex;

/**
 * @author 陈永佳 (yoojiachen@gmail.com)
 */
@FunctionalInterface
public interface DriverHandler {

    /**
     * 处理消息
     *
     * @param topic   EdgeXTopic
     * @param message 　接收到的消息
     * @throws Exception 　发生异常
     */
    void handle(String topic, Message message) throws Exception;
}
