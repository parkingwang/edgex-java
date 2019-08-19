package net.nextabc.edgex;

import lombok.extern.log4j.Log4j;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

/**
 * @author 陈哈哈 (yoojiachen@gmail.com)
 */
@Log4j
class Mqtt {

    static void setup(Globals globals, MqttConnectOptions opts) {
        opts.setKeepAliveInterval(globals.getMqttKeepAlive());
        opts.setAutomaticReconnect(globals.isMqttAutoReconnect());
        opts.setMaxReconnectDelay(globals.getMqttReconnectInterval() * 1000);
        opts.setConnectionTimeout(globals.getMqttConnectTimeout());
        opts.setCleanSession(globals.isMqttClearSession());
        final String username = globals.getMqttUsername();
        final String password = globals.getMqttPassword();
        if (null != username && !username.isEmpty()
                && null != password && !password.isEmpty()) {
            opts.setUserName(username);
            opts.setPassword(password.toCharArray());
        }
    }

    static Message createStateMessage(VirtualNodeState state) {
        final String nodeId = state.getNodeId();
        if (null != state.getUnionId()) {
            log.debug("使用虚拟使用自定义Uuid：" + state.getUnionId());
        } else {
            state = VirtualNodeState.builder()
                    .groupId(state.getGroupId())
                    .state(state.getState())
                    .nodeId(state.getNodeId())
                    .majorId(state.getMajorId())
                    .minorId(state.getMinorId())
                    .unionId(Message.makeUnionId(nodeId, state.getGroupId(), state.getMajorId(), state.getMinorId()))
                    .values(state.getValues())
                    .build();
        }
        return Message.newMessageByUnionId(
                state.getUnionId(),
                Codec.toJSON(state).getBytes(),
                0
        );
    }
}
