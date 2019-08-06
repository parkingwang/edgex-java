package net.nextabc.edgex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author 陈永佳 (yoojiachen@gmail.com)
 * @version 0.0.1
 */
public interface Driver extends LifeCycle, NodeId, Messaging, Publishable {

    /**
     * 处理消息
     *
     * @param handler 处理函数
     */
    void process(DriverHandler handler);

    /**
     * 发起一个消息请求，并获取响应消息。
     *
     * @param remoteNodeId 　目标Endpoint的地址
     * @param in           　请求消息
     * @param timeoutSec   　请求超时时间
     * @return 响应消息
     * @throws Exception 如果过程中发生错误，返回错误消息
     */
    Message execute(String remoteNodeId, Message in, int timeoutSec) throws Exception;

    /**
     * 发起一个异步请求
     *
     * @param remoteNodeId 远程节点ID
     * @param in           Message
     * @param cb           回调接口
     */
    void call(String remoteNodeId, Message in, Callback cb);

    /**
     * 添加Startup监听接口
     *
     * @param l 监听接口
     */
    void addStartupListener(OnStartupListener<Driver> l);

    /**
     * 添加Shutdown监听接口
     *
     * @param l 监听接口
     */
    void addShutdownListener(OnShutdownListener<Driver> l);

    ////

    @FunctionalInterface
    interface Callback {
        void onMessage(Message out);
    }

    ////

    final class Options {

        final List<String> eventTopics;
        final List<String> valueTopics;
        final List<String> customTopics;

        public Options() {
            this(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        }

        public Options(List<String> eventTopics, List<String> valueTopics, List<String> customTopics) {
            this.eventTopics = eventTopics == null ? Collections.emptyList() : eventTopics;
            this.valueTopics = valueTopics == null ? Collections.emptyList() : valueTopics;
            this.customTopics = customTopics == null ? Collections.emptyList() : customTopics;
        }

        public Options addEventTopic(String... topics) {
            eventTopics.addAll(Arrays.asList(topics));
            return this;
        }

        public Options addValueTopic(String... topics) {
            valueTopics.addAll(Arrays.asList(topics));
            return this;
        }

        public Options addCustomTopic(String... topics) {
            customTopics.addAll(Arrays.asList(topics));
            return this;
        }
    }
}
