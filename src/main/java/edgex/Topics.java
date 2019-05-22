package edgex;

import org.apache.log4j.Logger;

/**
 * @author 陈永佳 (yoojiachen@gmail.com)
 * @version 0.0.1
 */
final class Topics {

    private static final Logger log = Logger.getLogger(Topics.class);

    private static final String TRIGGER = "$EDGEX/EVENTS/${user-topic}";

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
