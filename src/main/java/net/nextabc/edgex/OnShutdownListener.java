package net.nextabc.edgex;

/**
 * @author 陈哈哈 (yoojiachen@gmail.com)
 * 在Shutdown函数调用时，此接口的相应方法会被调用。
 */
@FunctionalInterface
public interface OnShutdownListener<T> {
    /**
     * 在Shutdown函数调用后，开始时，调用此方法
     * @param host Host
     */
    void onBefore(T host);

    /**
     * 在Shutdown函数调用后，完成前，调用此方法
     * @param host Host
     */
    default void onAfter(T host) {
        // Shutdown 默认不实现 After方法
    }
}
