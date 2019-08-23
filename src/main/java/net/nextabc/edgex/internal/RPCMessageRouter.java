package net.nextabc.edgex.internal;

import lombok.extern.slf4j.Slf4j;
import net.nextabc.edgex.Message;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

/**
 * @author 陈哈哈 (yoojiachen@gmail.com)
 */
@Slf4j
public class RPCMessageRouter {

    private static final int MAX_MISSED = 10;

    private final Map<String, List<Registry>> registries = new ConcurrentHashMap<>();

    public void route(String topic, MqttMessage mqtt) {
        final List<Registry> list = registries.get(topic);
        if (list == null) {
            log.debug("接收到RPC响应事件，缺失处理对象。Topic= " + topic);
        } else {
            final Message msg = Message.parse(mqtt.getPayload());
            list.removeIf(r -> r.invoke(msg));
        }
    }

    public CompletableFuture<Message> register(String topic, Predicate<Message> predicate) {
        final CompletableFuture<Message> future = new CompletableFuture<>();
        registries.computeIfAbsent(topic, k -> new CopyOnWriteArrayList<>())
                .add(new Registry(future, predicate));
        return future;
    }

    ////


    private static class Registry {

        final CompletableFuture<Message> future;
        final Predicate<Message> predicate;
        private int missed = 0; // 同一Topic多次未收到响应，则自动删除

        private Registry(CompletableFuture<Message> future, Predicate<Message> predicate) {
            this.future = future;
            this.predicate = predicate;
        }

        boolean invoke(Message msg) {
            if (predicate.test(msg)) {
                future.complete(msg);
                return true;
            } else {
                return (missed += 1) >= MAX_MISSED;
            }
        }
    }
}
