package net.nextabc.edgex;

import lombok.extern.log4j.Log4j;

/**
 * @author 陈永佳 (yoojiachen@gmail.com)
 * @version 0.0.1
 */
@Log4j
final public class Topics {

    private static final String prefixEvent = "$EdgeX/events/";
    private static final String prefixNode = "$EdgeX/nodes/";

    private static final String tNodesStats = prefixNode + "stats/%s";
    private static final String tNodesOffline = prefixNode + "offline/%s/%s";
    private static final String tNodesEvent = prefixEvent + "${user-topic}";

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
    static String wrapTriggerEvents(String topic) {
        if (topic.startsWith("/")) {
            log.fatal("Topic MUST NOT starts with '/', was: " + topic);
        }
        return tNodesEvent.replace("${user-topic}", topic);
    }

    /**
     * 拆解原始MQTT Topic，返回EdgeX定义的Trigger Event Topic
     *
     * @param mqttRawTopic MQTT 原始Topic
     * @return Edgex定义的Topic
     */
    static String unwrapTriggerEvents(String mqttRawTopic) {
        if (mqttRawTopic.length() > prefixEvent.length()) {
            if (mqttRawTopic.startsWith(prefixEvent)) {
                return mqttRawTopic.substring(prefixEvent.length());
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
        return null != topic && (topic.startsWith(prefixEvent) || topic.startsWith(prefixNode));
    }
}
