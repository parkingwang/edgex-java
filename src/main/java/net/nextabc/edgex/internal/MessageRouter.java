package net.nextabc.edgex.internal;

import net.nextabc.edgex.Message;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author 陈哈哈 (yoojiachen@gmail.com)
 */
public class MessageRouter {

    private final ReentrantLock registryLock = new ReentrantLock();
    private final Map<String, List<LazySupplier>> registry = new HashMap<>();

    /**
     * 派发Topic和MQTT消息。
     *
     * @param topic Topic
     * @param mqtt  MQTT消息
     */
    public void dispatch(String topic, MqttMessage mqtt) {
        try {
            registryLock.lock();
            final Message msg = Message.parse(mqtt.getPayload());
            final List<LazySupplier> list = registry.get(topic);
            if (list != null) {
                final Iterator<LazySupplier> iterator = list.iterator();
                while (iterator.hasNext()) {
                    final LazySupplier ls = iterator.next();
                    if (ls.predicate.test(msg)) {
                        ls.set(msg);
                        iterator.remove();
                    }
                }
            }
        } finally {
            registryLock.unlock();
        }
    }

    /**
     * 注册Topic及检查条件。返回Future接口。
     * 当{@link MessageRouter#dispatch(String, MqttMessage)} 派发事件，并且符合检查条件时，CompletableFuture返回派发的事件。
     * 否则阻塞等待。
     *
     * @param topic     Topic
     * @param predicate 检查消息的条件
     * @return CompletableFuture
     */
    public CompletableFuture<Message> register(String topic, Predicate<Message> predicate) {
        final LazySupplier lazy = new LazySupplier(predicate);
        try {
            registryLock.lock();
            final List<LazySupplier> list = registry.computeIfAbsent(topic, k -> new ArrayList<>());
            list.add(lazy);
        } finally {
            registryLock.unlock();
        }
        return CompletableFuture.supplyAsync(lazy);
    }

    ////

    private static class LazySupplier implements Supplier<Message> {

        private final Predicate<Message> predicate;
        Message value;

        private LazySupplier(Predicate<Message> predicate) {
            this.predicate = predicate;
        }

        private synchronized void set(Message msg) {
            value = msg;
            notifyAll();
        }

        @Override
        public synchronized Message get() {
            try {
                wait();
            } catch (InterruptedException e) {/*ignore*/}
            return value;
        }
    }
}