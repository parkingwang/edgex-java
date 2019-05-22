package edgex;

/**
 * @author 陈永佳 (yoojiachen@gmail.com)
 * @version 0.0.1
 */
@FunctionalInterface
public interface MessageHandler {

    /**
     * 处理消息
     *
     * @param message 　接收到的消息
     * @throws Exception 　发生异常
     */
    void handle(Message message) throws Exception;
}
