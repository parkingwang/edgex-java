package net.nextabc.edgex.internal;

import net.nextabc.edgex.Message;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

public class MessageRouter {

    private final ReentrantLock lock = new ReentrantLock();
    private final Map<String, TopicHandler> handlers = new HashMap<>();

    public void dispatchThenRemove(String topic, MqttMessage msg) {
        try {
            lock.lock();
            final Message m = Message.parse(msg.getPayload());
            final TopicHandler th = handlers.get(topic);
            if (th != null) {
                th.trigger(m);
                if (th.handlers.isEmpty()) {
                    handlers.remove(topic);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public void register(String topic, Handler handler) {
        try {
            lock.lock();
            TopicHandler hs = handlers.get(topic);
            if (hs == null) {
                hs = new TopicHandler(topic);
                handlers.put(topic, hs);
            }
            hs.handlers.add(handler);
        } finally {
            lock.unlock();
        }
    }

    ////

    public interface Handler {
        boolean onMessage(Message msg);
    }

    ////

    private static class TopicHandler {

        final String topic;
        final List<Handler> handlers = new CopyOnWriteArrayList<>();

        private TopicHandler(String topic) {
            this.topic = topic;
        }

        void trigger(Message msg) {
            handlers.removeIf(h -> h.onMessage(msg));
        }
    }
}