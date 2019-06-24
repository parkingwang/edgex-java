package net.nextabc.edgex;

import org.apache.log4j.Logger;

/**
 * @author 陈永佳 (yoojiachen@gmail.com)
 * @version 0.0.1
 */
public class EdgeX {

    private static final Logger log = Logger.getLogger(EdgeX.class);

    private EdgeX() {
    }

    /**
     * 运行Application
     * @param application Application
     */
    public static void run(Application application) {
        final Context ctx = createContext();
        try {
            log.info("启动Service");
            application.run(ctx);
        } catch (Exception e) {
            log.error("Service出错", e);
        } finally {
            log.info("停止Service");
        }
    }

    /**
     * 创建Context
     * @return Context
     */
    public static Context createContext(){
        String broker = System.getenv(Context.ENV_KEY_MQTT_BROKER);
        if (null == broker || broker.isEmpty()) {
            broker = Context.DEFAULT_MQTT_BROKER;
        }
        return createContext(broker);
    }

    /**
     * 创建Context，指定Broker
     * @param  broker Broker Address
     * @return Context
     */
    public static Context createContext(String broker){
        return new ContextImpl(createDefaultGlobalScoped(broker));
    }

    public static GlobalScoped createDefaultGlobalScoped(String broker) {
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
                60,
                true,
                30,
                1000 * 60 * 60 * 6);
    }
}
