package edgex;

/**
 * @author 陈永佳 (yoojiachen@gmail.com)
 * @version 0.0.1
 */
interface LifeCycle {
    /**
     * Startup
     */
    void startup();

    /**
     * Shutdown
     */
    void shutdown();
}
