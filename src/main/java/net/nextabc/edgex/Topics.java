package net.nextabc.edgex;

import org.apache.log4j.Logger;

/**
 * @author 陈永佳 (yoojiachen@gmail.com)
 * @version 0.0.1
 */
final public class Topics {

    private static final Logger log = Logger.getLogger(Topics.class);

    private static final String tDevicesInspect = "$EDGEX/DEVICES/INSPECT";
    private static final String tDevicesOffline = "$EDGEX/DEVICES/OFFLINE/%s/%s";
    private static final String tDevicesAlive = "$EDGEX/DEVICES/ALIVE/%s/%s";
    private static final String tTrigger = "$EDGEX/EVENTS/${user-topic}";

    public static final String TopicDeviceInspect = "$EDGEX/DEVICES/INSPECT/#";
    public static final String TopicDeviceOffline = "$EDGEX/DEVICES/OFFLINE/#";
    public static final String TopicDeviceALIVE = "$EDGEX/DEVICES/ALIVE/#";
    public static final String TopicDeviceEvents = "$EDGEX/EVENTS/#";

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
        return tTrigger.replace("${user-topic}", topic);
    }

    static String topicOfOffline(String typeName, String name) {
        return String.format(tDevicesOffline, typeName, name);
    }

    static boolean isTopLevelTopic(String topic) {
        return topic.startsWith("$EDGEX/EVENTS/") || topic.startsWith("$EDGEX/DEVICES/");
    }
}
