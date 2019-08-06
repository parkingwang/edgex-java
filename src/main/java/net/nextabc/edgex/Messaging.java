package net.nextabc.edgex;

/**
 * @author 陈哈哈 (yoojiachen@gmail.com)
 */
public interface Messaging {

    /**
     * 返回内部消息流水号
     *
     * @return 消息流水号
     */
    int nextMessageSequenceId();

    /**
     * 根据指令VirtualId，使用内部NodeId，创建基于内部流水号的消息对象
     *
     * @param virtualId VirtualId
     * @param body      Body
     * @return 消息对象
     */
    Message nextMessageBy(String virtualId, byte[] body);

    /**
     * 根据指令VirtualId，使用内部NodeId，创建基于内部流水号的消息对象
     *
     * @param virtualNodeId Virtual Node Id
     * @param body          Body
     * @return 消息对象
     */
    Message nextMessageOf(String virtualNodeId, byte[] body);
}
