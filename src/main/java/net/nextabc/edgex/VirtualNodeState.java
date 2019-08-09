package net.nextabc.edgex;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * @author 陈哈哈 (yoojiachen@gmail.com)
 */
@Data
@Builder
public class VirtualNodeState {

    private final String nodeId;
    private final String uuid;
    private final String virtualId;
    private final String majorId;
    private final String minorId;
    private final String state;
    private final Map<String, Object> values;

    public VirtualNodeState(String nodeId, String uuid, String virtualId, String majorId, String minorId,
                            String state, Map<String, Object> values) {
        this.nodeId = nodeId;
        this.uuid = uuid;
        this.virtualId = virtualId;
        this.majorId = majorId;
        this.minorId = minorId;
        this.state = state;
        this.values = values;
    }
}
