package net.nextabc.edgex;

import lombok.extern.log4j.Log4j;

/**
 * @author 陈永佳 (yoojiachen@gmail.com)
 * @version 0.0.1
 */
@Log4j
final public class Topics {

    private static final String prefixNodes = "$EdgeX/nodes/";

    private static final String prefixEvents = "$EdgeX/events/";
    private static final String prefixValues = "$EdgeX/values/";
    private static final String prefixStats = "$EdgeX/stats/";
    private static final String prefixRequests = "$EdgeX/requests/";
    private static final String prefixReplies = "$EdgeX/replies/";

    /**
     * 节点Inspect事件的订阅Topic
     */
    public static final String SubscribeNodesInspect = prefixNodes + "inspect";

    /**
     * 节点发出Offline事件的订阅Topic
     */
    public static final String SubscribeNodesOffline = prefixNodes + "offline/#";

    /**
     * 节点发出TriggerEvent事件的订阅Topic
     */
    public static final String SubscribeNodesEvents = prefixEvents + "#";

    /**
     * 节点发出ValueEvent事件的订阅Topic
     */
    public static final String SubscribeNodesValues = prefixValues + "#";

    /**
     * 节点发出数据统计Event事件的订阅Topic
     */
    public static final String SubscribeNodesStats = prefixStats + "stats/#";

    private Topics() {
    }
    ////

    static String formatEvents(String topic) {
        checkTopic(topic);
        return prefixEvents + topic;
    }

    static String formatValues(String topic) {
        checkTopic(topic);
        return prefixValues + topic;
    }

    static String formatStats(String topic) {
        checkTopic(topic);
        return prefixStats + topic;
    }

    static String formatOffline(String typeName, String name) {
        return String.format(prefixNodes + "offline/%s/%s", typeName, name);
    }

    static String unwrapEdgeXTopic(String mqttRawTopic) {
        if (null != mqttRawTopic && (mqttRawTopic.startsWith("$EdgeX/"))) {
            if (mqttRawTopic.startsWith(prefixEvents)) {
                return mqttRawTopic.substring(prefixEvents.length());
            } else if (mqttRawTopic.startsWith(prefixStats)) {
                return mqttRawTopic.substring(prefixStats.length());
            } else if (mqttRawTopic.startsWith(prefixValues)) {
                return mqttRawTopic.substring(prefixValues.length());
            } else if (mqttRawTopic.startsWith(prefixNodes)) {
                return mqttRawTopic.substring(prefixNodes.length());
            } else {
                return mqttRawTopic;
            }
        } else {
            return mqttRawTopic;
        }
    }

    static String formatRepliesListen(String callerNodeId) {
        return String.format(prefixReplies + "%s/+/+", callerNodeId);
    }

    static String formatRequestSend(String executorNodeId, int seqId, String callerNodeId) {
        return String.format(prefixRequests + "%s/%d/%s", executorNodeId, seqId, callerNodeId);
    }

    static String formatRepliesFilter(String executorNodeId, int seqId, String callerNodeId) {
        return String.format(prefixReplies + "%s/%d/%s", callerNodeId, seqId, executorNodeId);
    }

    private static void checkTopic(String topic) {
        if (null == topic || topic.startsWith("/")) {
            log.fatal("Topic MUST NOT starts with '/', was: " + topic);
        }
    }

}
