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

    private static final String tNodesInspect = prefixNode + "inspect";
    private static final String tNodesStats = prefixNode + "stats/%s";
    private static final String tNodesOffline = prefixNode + "offline/%s/%s";
    private static final String tNodesEvent = prefixEvent + "${user-topic}";


    private Topics() {
    }
    ////

    /**
     * 创建trigger的Topic
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

    static String unwrapTriggerEvents(String topic) {
        if (topic.length() > prefixEvent.length()) {
            if (topic.startsWith(prefixEvent)) {
                return topic.substring(prefixEvent.length());
            } else {
                return topic;
            }
        } else {
            return topic;
        }
    }

    static String wrapOffline(String typeName, String name) {
        return String.format(tNodesOffline, typeName, name);
    }

    static String wrapStat(String nodeName) {
        return String.format(tNodesStats, nodeName);
    }

    static boolean isTopLevelTopic(String topic) {
        return null != topic && (topic.startsWith(prefixEvent) || topic.startsWith(prefixNode));
    }
}
