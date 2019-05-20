package edgex;

/**
 * @author 陈永佳 (yoojiachen@gmail.com)
 * @version 0.0.1
 */
@FunctionalInterface
public interface Runner {

    void run(Context ctx) throws Exception;
}
