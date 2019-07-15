package net.nextabc.edgex;

/**
 * @author 陈哈哈 (yoojiachen@gmail.com)
 * 在Startup函数调用时，此接口的相应方法会被调用。
 */
@FunctionalInterface
public interface OnStartupListener<T> {
    /**
     * 在Startup函数调用后，开始时，调用此方法
     */
    default void onBefore(T host) {
        // Startup 默认不实现 Before 方法
    }

    /**
     * 在Startup函数调用后，完成前，调用此方法
     */
    void onAfter(T host);
}
