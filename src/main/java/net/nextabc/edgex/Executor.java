package net.nextabc.edgex;

/**
 * @author 陈哈哈 (yoojiachen@gmail.com)
 */
public interface Executor {

    public Message execute(String endpointAddress, Message in, int timeoutSec) throws Exception;
}
