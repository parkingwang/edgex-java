package net.nextabc.edgex;

import org.apache.log4j.Logger;

/**
 * @author 陈永佳 (yoojiachen@gmail.com)
 * @version 0.0.1
 */
final public class Topics {

    private static final Logger log = Logger.getLogger(Topics.class);

    private static final String tNodesInspect = "$EDGEX/DEVICES/INSPECT";
    private static final String tNodesStat = "$EDGEX/devices/stats/%s";
    private static final String tNodesOffline = "$EDGEX/DEVICES/OFFLINE/%s/%s";
    private static final String tNodesEvent = "$EDGEX/EVENTS/${user-topic}";

    public static final String TopicNodesInspect = "$EDGEX/DEVICES/INSPECT/#";
    public static final String TopicNodesOffline = "$EDGEX/DEVICES/OFFLINE/#";
    public static final String TopicNodesEvent = "$EDGEX/EVENTS/#";

    private Topics() {
    }
    ////

    /**
     * 创建trigger的Topic
     *
     * @param topic Trigger定义的Topic
     * @return MQTT的Topic
     */
    static String topicOfTrigger(String topic) {
        if (topic.startsWith("/")) {
            log.fatal("Topic MUST NOT starts with '/', was: " + topic);
        }
        return tNodesEvent.replace("${user-topic}", topic);
    }

    static String topicOfOffline(String typeName, String name) {
        return String.format(tNodesOffline, typeName, name);
    }

    static String topicOfStat(String nodeName) {
        return String.format(tNodesStat, nodeName);
    }

    static boolean isTopLevelTopic(String topic) {
        return topic.startsWith("$EDGEX/EVENTS/") || topic.startsWith("$EDGEX/DEVICES/");
    }
}
