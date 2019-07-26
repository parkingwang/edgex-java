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
        return new Globals(
                Env.getString(EnvKeyMQBroker, Context.MqttBrokerDefault),
                Env.getString(EnvKeyMQUsername, null),
                Env.getString(EnvKeyMQPassword, null),
                Env.getInt(EnvKeyMQQOS, 1),
                Env.getBoolean(EnvKeyMQRetained, false),
                true,
                Env.getBoolean(EnvKeyMQCleanSession, true),
                3,
                3,
                5,
                3,
                120,
                true,
                30,
                1000 * 60 * 60 * 6);
    }

}
