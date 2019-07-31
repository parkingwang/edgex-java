package net.nextabc.edgex;

import lombok.extern.log4j.Log4j;

/**
 * @author 陈永佳 (yoojiachen@gmail.com)
 * @version 0.0.1
 */
@Log4j
final public class Topics {

    private static final String prefixEvent = "$EdgeX/events/";
    private static final String prefixValues = "$EdgeX/values/";
    private static final String prefixNode = "$EdgeX/nodes/";

    private static final String tNodesStats = prefixNode + "stats/%s";
    private static final String tNodesOffline = prefixNode + "offline/%s/%s";
    private static final String tNodesEvent = prefixEvent + "${user-topic}";
    private static final String tNodesValue = prefixValues + "${user-topic}";

    /**
     * 节点Inspect事件的订阅Topic
     */
    public static final String SubscribeNodesInspect = prefixNode + "inspect";

    /**
     * 节点发出Offline事件的订阅Topic
     */
    public static final String SubscribeNodeOffline = prefixNode + "offline/#";

    /**
     * 节点发出TriggerEvent事件的订阅Topic
     */
    public static final String SubscribeNodeEvent = prefixEvent + "#";

    /**
     * 节点发出ValueEvent事件的订阅Topic
     */
    public static final String SubscribeNodeValue = prefixValues + "#";

    /**
     * 节点发出数据统计Event事件的订阅Topic
     */
    public static final String SubscribeNodeStats = prefixNode + "stats/#";


    private Topics() {
    }
    ////

    /**
     * 构建trigger event的Topic
     *
     * @param topic Trigger定义的Topic
     * @return MQTT的Topic
     */
    static String wrapEvents(String topic) {
        if (topic.startsWith("/")) {
            log.fatal("Topic MUST NOT starts with '/', was: " + topic);
        }
        return tNodesEvent.replace("${user-topic}", topic);
    }

    /**
     * 还原MQTT的Topic为EdgeX定义的Topic
     *
     * @param mqttRawTopic MQTT原生Topic
     * @return Topic
     */
    static String unwrapTopic(String mqttRawTopic) {
        if (isTopLevelTopic(mqttRawTopic)) {
            if (mqttRawTopic.startsWith(prefixEvent)) {
                return unwrap0(prefixEvent, mqttRawTopic);
            } else if (mqttRawTopic.startsWith(prefixNode)) {
                return unwrap0(prefixNode, mqttRawTopic);
            } else if (mqttRawTopic.startsWith(prefixValues)) {
                return unwrap0(prefixValues, mqttRawTopic);
            } else {
                return mqttRawTopic;
            }
        } else {
            return mqttRawTopic;
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

    /**
     * 构建Offline事件Topic
     *
     * @param typeName 类型名称
     * @param nodeName 节点名称
     * @return Topic
     */
    static String wrapOffline(String typeName, String nodeName) {
        return String.format(tNodesOffline, typeName, nodeName);
    }

    /**
     * 构建数据统计事件Topic
     *
     * @param nodeName 节点名称
     * @return Topic
     */
    static String wrapStats(String nodeName) {
        return String.format(tNodesStats, nodeName);
    }

    /**
     * 返回是否为顶级事件Topic
     *
     * @param topic Topic
     * @return 是否为EdgeX的事件
     */
    static boolean isTopLevelTopic(String topic) {
        return null != topic && (topic.startsWith("$EdgeX/"));
    }
}
