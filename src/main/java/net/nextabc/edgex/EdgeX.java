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

    public static void run(Runner handler) {
        String broker = System.getenv("MQTT.broker");
        if (null == broker || broker.isEmpty()) {
            broker = "tcp://mqtt-broker.edgex.io:1883";
        }
        final GlobalScoped scoped = GlobalScoped.getDefault(broker);
        final Context ctx = new ContextImpl(scoped);
        try {
            log.info("启动Service");
            handler.run(ctx);
        } catch (Exception e) {
            log.error("Service出错", e);
        } finally {
            log.info("停止Service");
        }
    }
}
