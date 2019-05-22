package edgex;

import org.apache.log4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Class description goes here.
 *
 * @author 陈永佳 (yoojiachen@gmail.com)
 * @version 0.0.1
 */
public class Sample {

    private static final Logger log = Logger.getLogger(Sample.class);

    public static void main(String[] args) {
        EdgeX.run(ctx -> {
            Driver driver = ctx.newDriver(new Driver.Options(
                    "TestDriver",
                    new String[]{
                            "example/+",
                            "scheduled/+",
                    }
            ));

            driver.process(msg -> {
                // call execute to invoke endpoint device
                final long recv = Long.parseLong(new String(msg.body()));
                log.debug("Driver用时：" + (System.nanoTime() - recv) + "ns");

                final long start = System.currentTimeMillis();
                final Message rep;
                try {
                    rep = driver.execute("127.0.0.1:5570", msg, 1);
                    log.error("Execute用时" + (System.nanoTime() - start) + "ns");
                } catch (Exception e) {
                    log.error("Execute出错", e);
                    return;
                }
                log.debug(">> RESP: " + rep.body());
            });

            final ScheduledExecutorService threads = Executors.newSingleThreadScheduledExecutor();
            threads.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    final long start = System.currentTimeMillis();
                    final Message rep;
                    try {
                        rep = driver.execute("localhost:5570",
                                Message.fromString("SAMPLE", "AT+CLEAN"),
                                1);
                        log.debug("Execute用时:" + (System.currentTimeMillis() - start) + "ms");
                    } catch (Exception e) {
                        log.error("Execute出错", e);
                        return;
                    }
                    final String name = new String(rep.name());
                    final String body = new String(rep.body());
                    log.debug(">> Name: " + name + ", Body: " + body);
                }
            }, 1, 3, TimeUnit.SECONDS);

            // Wait to shutdown
            try {
                driver.startup();
                ctx.termChan().await();
            } finally {
                threads.shutdown();
                driver.shutdown();
                log.debug("服务终止");
            }
        });
    }
}
