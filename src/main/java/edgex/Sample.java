package edgex;

import java.util.Map;

/**
 * Class description goes here.
 *
 * @author 陈永佳 (yoojiachen@gmail.com)
 * @version 0.0.1
 */
public class Sample {

    public static void main(String[] args) {
        EdgeX.run(ctx -> {
            Map<String, Object> conf = ctx.loadConfig();

            Driver driver = ctx.newDriver(new Driver.Options(
                    "TestDriver",
                    new String[]{
                            "sample/app"
                    }
            ));

            driver.process(msg -> {
                // call execute to invoke endpoint device
            });
        });
    }
}
