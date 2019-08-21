package net.nextabc.edgex.extra;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * @author 陈永佳 (yoojiachen@gmail.com)
 * @version 0.0.1
 */
@Data
@Builder
public class VirtualNodeProperties {

    private final String unionId;
    private final String groupId;
    private final String majorId;
    private final String minorId;
    private final String description;
    private final boolean virtual;
    private final Map<String, String> stateCommands;
    private final Map<String, String> attrs;

    public VirtualNodeProperties(String unionId, String groupId, String majorId, String minorId,
                          String description, boolean virtual,
                          Map<String, String> stateCommands, Map<String, String> attrs) {
        this.unionId = unionId;
        this.groupId = groupId;
        this.majorId = majorId;
        this.minorId = minorId;
        this.description = description;
        this.virtual = virtual;
        this.stateCommands = stateCommands;
        this.attrs = attrs;
    }
}
