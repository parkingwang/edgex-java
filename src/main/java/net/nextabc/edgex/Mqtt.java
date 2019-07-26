package net.nextabc.edgex;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

/**
 * @author 陈哈哈 (yoojiachen@gmail.com)
 */
class Mqtt {

    static void setup(Globals globals, MqttConnectOptions opts) {
        opts.setKeepAliveInterval(globals.getMqttKeepAlive());
        opts.setAutomaticReconnect(globals.isMqttAutoReconnect());
        opts.setMaxReconnectDelay(globals.getMqttReconnectInterval() * 1000);
        opts.setConnectionTimeout(globals.getMqttConnectTimeout());
        opts.setCleanSession(globals.isMqttClearSession());
        final String username = globals.getMqttUsername();
        final String password = globals.getMqttPassword();
        if (null != username && !username.isEmpty()
                && null != password && !password.isEmpty()) {
            opts.setUserName(username);
            opts.setPassword(password.toCharArray());
        }
    }
}
