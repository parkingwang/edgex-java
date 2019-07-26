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

    String EnvKeyMQBroker = "EDGEX_MQTT_BROKER";
    String EnvKeyMQUsername = "EDGEX_MQTT_USERNAME";
    String EnvKeyMQPassword = "EDGEX_MQTT_PASSWORD";
    String EnvKeyMQQOS = "EDGEX_MQTT_QOS";
    String EnvKeyMQRetained = "EDGEX_MQTT_RETAINED";
    String EnvKeyMQCleanSession = "EDGEX_MQTT_CLEAN_SESSION";
    String EnvKeyConfig = "EDGEX_CONFIG";
    String EnvKeyLogVerbose = "EDGEX_LOG_VERBOSE";
    String EnvKeyGrpcAddress = "EDGEX_GRPC_ADDRESS";

    String MqttBrokerDefault = "tcp://mqtt-broker.edgex.io:1883";

    String DefaultConfName = "application.toml";
    String DefaultConfDir = "/etc/edgex/application.toml";

    /**
     * 初始化Context
     *
     * @param nodeName Node Name
     */
    void initial(String nodeName);

    /**
     * 使用默认配置结构来初始化Context
     *
     * @param config 配置
     */
    void initialWithConfig(Map<String, Object> config);

    /**
     * 销毁Context
     */
    void destroy();

    /**
     * 返回节点名称
     *
     * @return 节点名称
     */
    String nodeName();

    /**
     * 加载配置文件
     *
     * @return 非空配置文件对象
     */
    default Map<String, Object> loadConfig() {
        return loadConfigByName(DefaultConfName);
    }

    /**
     * 加载指定文件名的配置文件
     *
     * @return 非空配置文件对象
     */
    Map<String, Object> loadConfigByName(String fileName);

    /**
     * 创建Driver
     *
     * @param opts 参数
     * @return Driver对象
     */
    Driver newDriver(Driver.Options opts);

    /**
     * 创建Executor
     *
     * @return Executor对象
     */
    Executor newExecutor();

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
