package net.nextabc.edgex;

/**
 * @author 陈哈哈 (yoojiachen@gmail.com)
 */
@FunctionalInterface
public interface Application {
    /**
     * Run
     *
     * @param ctx Context
     * @throws Exception If error
     */
    void run(Context ctx) throws Exception;
}
