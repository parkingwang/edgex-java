package edgex;

/**
 * @author 陈永佳 (yoojiachen@gmail.com)
 * @version 0.0.1
 */
@FunctionalInterface
public interface Runner {

    /**
     * Run
     *
     * @param ctx Context
     * @throws Exception If error
     */
    void run(Context ctx) throws Exception;
}
