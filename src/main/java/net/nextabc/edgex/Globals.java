package net.nextabc.edgex;

import lombok.Builder;
import lombok.Data;

/**
 * 全局配置
 *
 * @author 陈永佳 (yoojiachen@gmail.com)
 * @version 0.0.1
 */
@Data
@Builder
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

    private boolean logVerbose;

    private long nodeDataCenterId;
    private long nodeWorkerId;

}
