package net.nextabc.edgex.sample;

import lombok.extern.log4j.Log4j;
import net.nextabc.edgex.Driver;
import net.nextabc.edgex.EdgeX;
import net.nextabc.edgex.Message;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author 陈哈哈 (yoojiachen@gmail.com)
 */
@Log4j
public class TestDriver {

    private static final String NODE_ID = "TestDriverJava";

    public static void main(String[] args) {

        EdgeX.run(ctx -> {
            ctx.initial(NODE_ID);
            final Driver driver = ctx.newDriver(new Driver.Options());

            final ScheduledExecutorService ticker = Executors.newScheduledThreadPool(4);
            ticker.scheduleAtFixedRate(() -> {
                try {
                    Message msg = driver.execute(
                            "DEV-ENDPOINT",
                            "main",
                            "main",
                            "",
                            "Hello".getBytes(),
                            driver.generateEventId(),
                            3);
                    System.out.println("########## Exec响应：" + new String(msg.body()));
                } catch (Exception e) {
                    log.error("Exec调用出错: " + e);
                }
            }, 1, 3, TimeUnit.SECONDS);

            try {
                driver.startup();
                ctx.termAwait();
            } finally {
                driver.shutdown();
            }
        });
    }
}
