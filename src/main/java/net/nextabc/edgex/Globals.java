package net.nextabc.edgex;

import lombok.Getter;
import lombok.Setter;

/**
 * 全局配置
 *
 * @author 陈永佳 (yoojiachen@gmail.com)
 * @version 0.0.1
 */
@Getter
@Setter
public class Globals {

    private String mqttBroker;
    private String mqttUsername;
    private String mqttPassword;
    private int mqttQoS;
    private boolean mqttRetained;
    private boolean mqttAutoReconnect;
    private boolean mqttClearSession;
    private int mqttKeepAlive;
    private int mqttPingTimeout;
    private int mqttConnectTimeout;
    private int mqttReconnectInterval;
    private int mqttMaxRetry;

    private boolean grpcKeepAlive;
    private int grpcKeepAliveTimeoutSec;
    private int grpcConnectionCacheTTL;

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
