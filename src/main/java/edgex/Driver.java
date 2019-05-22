package edgex;

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

        final String name;
        final String[] topics;

        public Options(String name, String[] topics) {
            this.name = name;
            this.topics = topics;
        }
    }
}
