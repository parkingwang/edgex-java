package edgex;

/**
 * 全局配置
 *
 * @author 陈永佳 (yoojiachen@gmail.com)
 * @version 0.0.1
 */
public class GlobalScoped {

    final String mqttBroker;
    final int mqttQoS;
    final boolean mqttRetained;
    final boolean mqttAutoReconnect;
    final boolean mqttClearSession;
    final int mqttKeepAlive;
    final int mqttPingTimeout;
    final int mqttConnectTimeout;
    final int mqttReconnectInterval;
    final int mqttMaxRetry;

    public GlobalScoped(String broker, int qos, boolean retained, boolean autoReconnect,
                        boolean clearSession, int keepAlive, int pingTimeout, int connectTimeout, int reconnectInterval,
                        int maxRetry) {
        this.mqttBroker = broker;
        this.mqttQoS = qos;
        this.mqttRetained = retained;
        this.mqttAutoReconnect = autoReconnect;
        this.mqttClearSession = clearSession;
        this.mqttKeepAlive = keepAlive;
        this.mqttPingTimeout = pingTimeout;
        this.mqttConnectTimeout = connectTimeout;
        this.mqttReconnectInterval = reconnectInterval;
        this.mqttMaxRetry = maxRetry;
    }

    public static GlobalScoped getDefault(String broker) {
        return new GlobalScoped(
                broker,
                2,
                false,
                true,
                true,
                3,
                3,
                5,
                3,
                60);
    }
}
