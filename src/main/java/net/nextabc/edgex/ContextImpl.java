package net.nextabc.edgex;

import com.moandjiezana.toml.Toml;
import lombok.extern.log4j.Log4j;
import net.nextabc.kit.HashMap;
import net.nextabc.kit.ImmutableMap;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Stream;

/**
 * Context Impl
 *
 * @author 陈永佳 (yoojiachen@gmail.com)
 * @version 0.0.1
 */
@Log4j
final class ContextImpl implements Context {

    private Globals globals;
    private MqttClient mqttClient;
    private String nodeId;
    private final CountDownLatch shutdown = new CountDownLatch(1);

    ContextImpl(Globals globals) {
        this.globals = globals;
        Runtime.getRuntime().addShutdownHook(new Thread(shutdown::countDown));
    }

    @Override
    public void initial(String nodeId) {
        this.initialWithConfig(HashMap.of("NodeId", nodeId));
    }

    @Override
    public void initialWithConfig(Map<String, Object> in) {
        final ImmutableMap config = ImmutableMap.wrap(in);
        nodeId = config.getString("NodeId");
        if (nodeId == null || nodeId.isEmpty() || nodeId.contains("/")) {
            log.fatal("非法格式的NodeId: " + nodeId);
        }
        // 更新Globals
        final ImmutableMap g = config.getImmutableMap("Globals");
        if (g.isNotEmpty()) {
            if (g.containsKey("LogVerbose")) {
                globals.setLogVerbose(g.getBoolean("LogVerbose"));
            }
            if (g.containsKey("MqttBroker")) {
                globals.setMqttBroker(g.getString("MqttBroker"));
            }
            if (g.containsKey("MqttUsername")) {
                globals.setMqttUsername(g.getString("MqttUsername"));
            }
            if (g.containsKey("MqttPassword")) {
                globals.setMqttPassword(g.getString("MqttPassword"));
            }
            if (g.containsKey("MqttQoS")) {
                globals.setMqttQoS(g.getInt("MqttQoS"));
            }
            if (g.containsKey("MqttRetained")) {
                globals.setMqttRetained(g.getBoolean("MqttRetained"));
            }
            if (g.containsKey("MqttAutoReconnect")) {
                globals.setMqttAutoReconnect(g.getBoolean("MqttAutoReconnect"));
            }
            if (g.containsKey("MqttClearSession")) {
                globals.setMqttClearSession(g.getBoolean("MqttClearSession"));
            }
            if (g.containsKey("MqttKeepAlive")) {
                globals.setMqttKeepAlive(g.getInt("MqttKeepAlive"));
            }
            if (g.containsKey("MqttConnectTimeout")) {
                globals.setMqttConnectTimeout(g.getInt("MqttConnectTimeout"));
            }
            if (g.containsKey("MqttReconnectInterval")) {
                globals.setMqttReconnectInterval(g.getInt("MqttReconnectInterval"));
            }
            if (g.containsKey("MqttMaxRetry")) {
                globals.setMqttMaxRetry(g.getInt("MqttMaxRetry"));
            }
        }

        final String clientId = "EXNode:" + nodeId;
        final MemoryPersistence mp = new MemoryPersistence();
        try {
            this.mqttClient = new MqttClient(this.globals.getMqttBroker(), clientId, mp);
        } catch (MqttException e) {
            log.fatal("Mqtt客户端错误", e);
        }
        log.info("Mqtt客户端连接Broker: " + globals.getMqttBroker());
        final MqttConnectOptions opts = new MqttConnectOptions();
        opts.setWill(Topics.PublishNodesOffline, nodeId.getBytes(), 0, true);
        Mqtt.setup(this.globals, opts);
        for (int i = 0; i < globals.getMqttMaxRetry(); i++) {
            try {
                this.mqttClient.connect(opts);
                break;
            } catch (MqttException e) {
                log.error("Mqtt客户端出错：", e);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    break;
                }
            }
        }
        if (!this.mqttClient.isConnected()) {
            log.fatal("Mqtt客户端无法连接Broker");
        } else {
            log.info("Mqtt客户端连接成功: " + clientId);
        }
    }

    @Override
    public void destroy() {
        try {
            this.mqttClient.disconnect(500);
            this.mqttClient.close(true);
        } catch (MqttException e) {
            log.error("Mqtt客户端断开连接出错:", e);
        }
    }

    @Override
    public String nodeId() {
        return nodeId;
    }

    @Override
    public Map<String, Object> loadConfig() {
        return loadConfigByName(DefaultConfName);
    }

    @Override
    public Map<String, Object> loadConfigByName(String fileName) {
        final File file = Stream.of(fileName, DefaultConfDir + fileName, System.getenv(EnvKeyConfig))
                .filter(f -> f != null && !f.isEmpty())
                .map(File::new)
                .filter(File::exists)
                .findFirst()
                .orElse(null);
        if (null == file) {
            log.fatal("未找到任何配置文件");
            return Collections.emptyMap();
        } else {
            log.info("加载配置文件：" + file);
            return new Toml().read(file).toMap();
        }
    }

    @Override
    public Driver newDriver(Driver.Options opts) {
        checkCtx();
        return new DriverImpl(nodeId, mqttClient, globals, opts);
    }

    @Override
    public CountDownLatch termChan() {
        return shutdown;
    }

    @Override
    public void termAwait() {
        try {
            this.termChan().await();
        } catch (Exception e) {
            log.error("等待终止信号超时", e);
        }
    }

    private void checkCtx() {
        if (this.mqttClient == null) {
            log.fatal("Context未初始化");
        }
    }
}
