package net.nextabc.edgex;

import org.apache.log4j.Logger;

/**
 * @author 陈永佳 (yoojiachen@gmail.com)
 * @version 0.0.1
 */
final class Topics {

    private static final Logger log = Logger.getLogger(Topics.class);

    private static final String TRIGGER = "$EDGEX/EVENTS/${user-topic}";

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
        return TRIGGER.replace("${user-topic}", topic);
    }
}
