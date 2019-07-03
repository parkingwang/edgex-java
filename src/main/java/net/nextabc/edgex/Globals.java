package net.nextabc.edgex;

/**
 * 全局配置
 *
 * @author 陈永佳 (yoojiachen@gmail.com)
 * @version 0.0.1
 */
public class Globals {

    final String mqttBroker;
    final String mqttUsername;
    final String mqttPassword;
    final int mqttQoS;
    final boolean mqttRetained;
    final boolean mqttAutoReconnect;
    final boolean mqttClearSession;
    final int mqttKeepAlive;
    final int mqttPingTimeout;
    final int mqttConnectTimeout;
    final int mqttReconnectInterval;
    final int mqttMaxRetry;

    final boolean grpcKeepAlive;
    final int grpcKeepAliveTimeoutSec;
    final int grpcConnectionCacheTTL;

    public Globals(String broker, String mqttUsername, String mqttPassword, int qos, boolean retained,
                   boolean autoReconnect, boolean clearSession, int keepAlive, int pingTimeout, int connectTimeout, int reconnectInterval, int maxRetry,
                   boolean grpcKeepAlive, int grpcKeepAliveTimeoutSec, int grpcConnectionCacheTTL) {
        this.mqttBroker = broker;
        this.mqttUsername = mqttUsername;
        this.mqttPassword = mqttPassword;
        this.mqttQoS = qos;
        this.mqttRetained = retained;
        this.mqttAutoReconnect = autoReconnect;
        this.mqttClearSession = clearSession;
        this.mqttKeepAlive = keepAlive;
        this.mqttPingTimeout = pingTimeout;
        this.mqttConnectTimeout = connectTimeout;
        this.mqttReconnectInterval = reconnectInterval;
        this.mqttMaxRetry = maxRetry;
        this.grpcKeepAlive = grpcKeepAlive;
        this.grpcKeepAliveTimeoutSec = grpcKeepAliveTimeoutSec;
        this.grpcConnectionCacheTTL = grpcConnectionCacheTTL;
    }

}