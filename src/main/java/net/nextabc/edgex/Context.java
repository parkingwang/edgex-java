package net.nextabc.edgex;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;

/**
 * Class description goes here.
 *
 * @author 陈永佳 (yoojiachen@gmail.com)
 * @version 0.0.1
 */
public interface Context {

    String APP_CONF_ENV_KEY = "EdgeX.Config";
    String DEFAULT_CONF_NAME = "application.toml";
    String DEFAULT_CONF_FILE = "/etc/edgex/application.toml";

    /**
     * 加载配置文件
     *
     * @return 非空配置文件对象
     */
    Map<String, Object> loadConfig();

    /**
     * 创建Driver
     *
     * @param opts 参数
     * @return Driver对象
     */
    Driver newDriver(Driver.Options opts);

    /**
     * 返回终止信号
     *
     * @return 返回终止信号
     * @throws TimeoutException If timeout
     */
    CountDownLatch termChan() throws TimeoutException;

    /**
     * 等待终止信号
     */
    void termAwait();

}
