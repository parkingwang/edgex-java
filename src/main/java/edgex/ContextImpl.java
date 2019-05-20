package edgex;

import com.moandjiezana.toml.Toml;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

/**
 * Context Impl
 *
 * @author 陈永佳 (yoojiachen@gmail.com)
 * @version 0.0.1
 */
final class ContextImpl implements Context {

    private static final Logger log = Logger.getLogger(ContextImpl.class);

    private final GlobalScoped scoped;
    private String serviceName;

    ContextImpl(GlobalScoped scoped) {
        this.scoped = scoped;
    }

    @Override
    public Map<String, Object> loadConfig() {
        final File file = Stream.of(DEFAULT_CONF_NAME, DEFAULT_CONF_FILE, System.getenv(APP_CONF_ENV_KEY))
                .map(File::new)
                .filter(File::exists)
                .findFirst()
                .orElse(null);
        if (null == file) {
            log.fatal("未设置任何文件");
            return Collections.emptyMap();
        } else {
            log.info("加载配置文件：" + file);
            return new Toml().read(file).toMap();
        }
    }

    @Override
    public Driver newDriver(Driver.Options opts) {
        this.serviceName = "Driver";
        checkRequired(opts.name, "Trigger.Name MUST be specified");
        checkRequired(opts.topics, "Trigger.Topic MUST be specified");
        return new DriverImpl(this.scoped, opts);
    }

    @Override
    public CountDownLatch termAwait() throws TimeoutException {
        return null;
    }

    private void checkRequired(Object item, String message) {
        if (null == item) {
            log.fatal(message);
        }
        if (item instanceof String && ((String) item).isEmpty()) {
            log.fatal(message);
        }
        if (item instanceof String[] && ((String[]) item).length == 0) {
            log.fatal(message);
        }
    }


}
