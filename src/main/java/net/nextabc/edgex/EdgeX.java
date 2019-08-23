package net.nextabc.edgex;

import lombok.extern.log4j.Log4j;

import static net.nextabc.edgex.Context.*;

/**
 * @author 陈永佳 (yoojiachen@gmail.com)
 * @version 0.0.1
 */
@Log4j
public class EdgeX {

    private EdgeX() {
    }

    /**
     * 运行Application
     *
     * @param application Application
     */
    public static void run(Application application) {
        final Context ctx = createContext();
        try {
            log.info("启动EdgeX-App");
            application.run(ctx);
        } catch (Exception e) {
            log.error("EdgeX-App出错", e);
        } finally {
            ctx.destroy();
            log.info("停止EdgeX-App");
        }
    }

    /**
     * 创建Context
     *
     * @return Context
     */
    public static Context createContext() {
        return new ContextImpl(createDefaultGlobals());
    }

    public static Globals createDefaultGlobals() {
        return Globals.builder()
                .nodeDataCenterId(Env.getInt(EnvKeyDataCenterId, 0))
                .nodeWorkerId(Env.getInt(EnvKeyWorkerId, 0))
                .logVerbose(Env.getBoolean(EnvKeyLogVerbose, false))
                .mqttBroker(Env.getString(EnvKeyMQBroker, Context.MqttBrokerDefault))
                .mqttUsername(Env.getString(EnvKeyMQUsername, null))
                .mqttPassword(Env.getString(EnvKeyMQPassword, null))
                .mqttQoS(Env.getInt(EnvKeyMQQoS, 0))
                .mqttRetained(Env.getBoolean(EnvKeyMQRetained, false))
                .mqttClearSession(Env.getBoolean(EnvKeyMQCleanSession, true))
                .mqttAutoReconnect(true)
                .mqttKeepAlive(3)
                .mqttPingTimeout(3)
                .mqttConnectTimeout(5)
                .mqttReconnectInterval(5)
                .mqttMaxRetry(30)
                .build();
    }

}
