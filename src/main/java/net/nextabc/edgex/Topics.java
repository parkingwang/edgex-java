package net.nextabc.edgex;

import lombok.extern.log4j.Log4j;

/**
 * @author 陈永佳 (yoojiachen@gmail.com)
 * @version 0.0.1
 */
@Log4j
final public class Topics {

    private static final String prefixProperties = "$EdgeX/properties/";
    private static final String prefixEvents = "$EdgeX/events/";
    private static final String prefixValues = "$EdgeX/values/";
    private static final String prefixStatistics = "$EdgeX/statistics/";
    private static final String prefixStates = "$EdgeX/states/";
    private static final String prefixRequests = "$EdgeX/requests/";
    private static final String prefixReplies = "$EdgeX/replies/";

    /**
     * 节点Inspect事件的订阅Topic
     */
    public static final String SubscribeProperties = prefixProperties + "#";

    /**
     * 节点发出TriggerEvent事件的订阅Topic
     */
    public static final String SubscribeEvents = prefixEvents + "#";

    /**
     * 节点发出ValueEvent事件的订阅Topic
     */
    public static final String SubscribeValues = prefixValues + "#";

    /**
     * 节点发出数据统计事件的订阅Topic
     */
    public static final String SubscribeStatistics = prefixStatistics + "#";

    private Topics() {
    }
    ////

    public static String formatEvents(String exTopic) {
        checkTopic(exTopic);
        return prefixEvents + exTopic;
    }

    public static String formatValues(String exTopic) {
        checkTopic(exTopic);
        return prefixValues + exTopic;
    }

    public static String formatState(String exTopic) {
        checkTopic(exTopic);
        return prefixStates + exTopic;
    }

    public static String formatStatistics(String nodeId) {
        checkTopic(nodeId);
        return prefixStatistics + nodeId;
    }

    static String unwrapEdgeXTopic(String mqttRawTopic) {
        if (null != mqttRawTopic) {
            if (mqttRawTopic.startsWith(prefixEvents)) {
                return mqttRawTopic.substring(prefixEvents.length());
            } else if (mqttRawTopic.startsWith(prefixStatistics)) {
                return mqttRawTopic.substring(prefixStatistics.length());
            } else if (mqttRawTopic.startsWith(prefixValues)) {
                return mqttRawTopic.substring(prefixValues.length());
            } else if (mqttRawTopic.startsWith(prefixProperties)) {
                return mqttRawTopic.substring(prefixProperties.length());
            } else if (mqttRawTopic.startsWith(prefixStates)) {
                return mqttRawTopic.substring(prefixStates.length());
            } else {
                return mqttRawTopic;
            }
        } else {
            return null;
        }
    }

    static String formatRepliesListen(String callerNodeId) {
        return prefixReplies + callerNodeId + "/+";
    }

    static String formatRepliesFilter(String executorNodeId, String callerNodeId) {
        return prefixReplies + callerNodeId + "/" + executorNodeId;
    }

    static String formatRequestSend(String executorNodeId, String callerNodeId) {
        return prefixRequests + executorNodeId + "/" + callerNodeId;
    }

    private static void checkTopic(String topic) {
        if (null == topic || topic.startsWith("/")) {
            log.fatal("Topic MUST NOT starts with '/', was: " + topic);
        }
    }

}
