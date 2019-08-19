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

    private final String unionId;
    private final String nodeId;
    private final String groupId;
    private final String majorId;
    private final String minorId;
    private final String state;
    private final Map<String, Object> values;

    public VirtualNodeState(String unionId, String nodeId, String groupId, String majorId, String minorId,
                            String state, Map<String, Object> values) {
        this.unionId = unionId;
        this.nodeId = nodeId;
        this.groupId = groupId;
        this.majorId = majorId;
        this.minorId = minorId;
        this.state = state;
        this.values = values;
    }
}
