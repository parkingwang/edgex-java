package net.nextabc.edgex;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

/**
 * @author 陈哈哈 (yoojiachen@gmail.com)
 */
class Mqtt {
    
    static void setup(Globals globals, MqttConnectOptions opts) {
        opts.setKeepAliveInterval(globals.mqttKeepAlive);
        opts.setAutomaticReconnect(globals.mqttAutoReconnect);
        opts.setMaxReconnectDelay(globals.mqttReconnectInterval * 1000);
        opts.setConnectionTimeout(globals.mqttConnectTimeout);
        opts.setCleanSession(globals.mqttClearSession);
        if (null != globals.mqttUsername && !globals.mqttUsername.isEmpty()
                && null != globals.mqttPassword && !globals.mqttPassword.isEmpty()) {
            opts.setUserName(globals.mqttUsername);
            opts.setPassword(globals.mqttPassword.toCharArray());
        }
    }
}
