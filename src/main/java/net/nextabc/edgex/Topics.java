package net.nextabc.edgex;

import lombok.extern.log4j.Log4j;

/**
 * @author 陈永佳 (yoojiachen@gmail.com)
 * @version 0.0.1
 */
@Log4j
final public class Topics {

    private static final String prefixEvents = "$EdgeX/events/";
    private static final String prefixValues = "$EdgeX/values/";
    private static final String prefixNodes = "$EdgeX/nodes/";
    private static final String prefixStats = "$EdgeX/stats/";

    /**
     * 节点Inspect事件的订阅Topic
     */
    public static final String SubscribeNodesInspect = prefixStats + "inspect";

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

    /**
     * 构建trigger event的Topic
     *
     * @param topic Trigger定义的Topic
     * @return MQTT的Topic
     */
    public static String formatEvents(String topic) {
        checkTopic(topic);
        return String.format("$EdgeX/events/%s", topic);
    }

    /**
     * 构建trigger values的Topic
     *
     * @param topic Trigger定义的Topic
     * @return MQTT的Topic
     */
    public static String formatValues(String topic) {
        checkTopic(topic);
        return String.format("$EdgeX/values/%s", topic);
    }

    static String formatOffline(String typeName, String name) {
        return String.format("$EdgeX/nodes/offline/%s/%s", typeName, name);
    }

    /**
     * 还原MQTT的Topic为EdgeX定义的Topic
     *
     * @param mqttRawTopic MQTT原生Topic
     * @return Topic
     */
    public static String unwrapEdgeXTopic(String mqttRawTopic) {
        if (isEdgeX(mqttRawTopic)) {
            if (mqttRawTopic.startsWith(prefixEvents)) {
                return unwrap0(prefixEvents, mqttRawTopic);
            } else if (mqttRawTopic.startsWith(prefixStats)) {
                return unwrap0(prefixStats, mqttRawTopic);
            } else if (mqttRawTopic.startsWith(prefixValues)) {
                return unwrap0(prefixValues, mqttRawTopic);
            } else {
                return mqttRawTopic;
            }
        } else {
            return mqttRawTopic;
        }
    }

    static String formatRepliesListen(String callerNodeId) {
        return String.format("$EdgeX/replies/%s/+/+", callerNodeId);
    }

    static String formatRequestSend(String executorNodeId, int seqId, String callerNodeId) {
        return String.format("$EdgeX/requests/%s/%d/%s", executorNodeId, seqId, callerNodeId);
    }

    static String formatRepliesFilter(String executorNodeId, int seqId, String callerNodeId) {
        return String.format("$EdgeX/replies/%s/%d/%s", callerNodeId, seqId, executorNodeId);
    }

    /**
     * 返回是否为EdgeX系统事件Topic
     *
     * @param topic Topic
     * @return 是否为EdgeX的事件
     */
    static boolean isEdgeX(String topic) {
        return null != topic && (topic.startsWith("$EdgeX/"));
    }

    private static void checkTopic(String topic) {
        if (null == topic || topic.startsWith("/")) {
            log.fatal("Topic MUST NOT starts with '/', was: " + topic);
        }
    }

    private static String unwrap0(String prefix, String mqttRawTopic) {
        if (mqttRawTopic.length() > prefix.length()) {
            if (mqttRawTopic.startsWith(prefix)) {
                return mqttRawTopic.substring(prefix.length());
            } else {
                return mqttRawTopic;
            }
        } else {
            return mqttRawTopic;
        }
    }

}
