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

    public GlobalScoped(String broker, int qos, boolean retained, boolean autoReconnect,
                        boolean clearSession, int keepAlive, int pingTimeout, int connectTimeout, int reconnectInterval) {
        this.mqttBroker = broker;
        this.mqttQoS = qos;
        this.mqttRetained = retained;
        this.mqttAutoReconnect = autoReconnect;
        this.mqttClearSession = clearSession;
        this.mqttKeepAlive = keepAlive;
        this.mqttPingTimeout = pingTimeout;
        this.mqttConnectTimeout = connectTimeout;
        this.mqttReconnectInterval = reconnectInterval;
    }
}
