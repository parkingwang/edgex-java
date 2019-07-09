package net.nextabc.edgex;

import org.apache.log4j.Logger;

/**
 * @author 陈永佳 (yoojiachen@gmail.com)
 * @version 0.0.1
 */
final public class Topics {

    private static final Logger log = Logger.getLogger(Topics.class);

    private static final String tNodesInspect = "$EdgeX/nodes/inspect";
    private static final String tNodesStats = "$EdgeX/nodes/stats/%s";
    private static final String tNodesOffline = "$EdgeX/nodes/offline/%s/%s";
    private static final String tNodesEvent = "$EdgeX/events/${user-topic}";

    public static final String TopicNodesInspect = "$EdgeX/nodes/inspect";
    public static final String TopicNodesOffline = "$EdgeX/nodes/offline/#";
    public static final String TopicNodesEvent = "$EdgeX/events/#";

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
        return String.format(tNodesStats, nodeName);
    }

    static boolean isTopLevelTopic(String topic) {
        return topic.startsWith("$EdgeX/events/") || topic.startsWith("$EdgeX/nodes/");
    }
}
