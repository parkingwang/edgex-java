package net.nextabc.edgex;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * @author 陈永佳 (yoojiachen@gmail.com)
 * @version 0.0.1
 */
@Data
@Builder
class VirtualNodeProperties {

    private final String uuid;
    private final String virtualId;
    private final String majorId;
    private final String minorId;
    private final String description;
    private final boolean virtual;
    private final Map<String, String> stateCommands;
    private final Map<String, String> attrs;

    VirtualNodeProperties(String uuid, String virtualId, String majorId, String minorId,
                          String description, boolean virtual,
                          Map<String, String> stateCommands, Map<String, String> attrs) {
        this.uuid = uuid;
        this.virtualId = virtualId;
        this.majorId = majorId;
        this.minorId = minorId;
        this.description = description;
        this.virtual = virtual;
        this.stateCommands = stateCommands;
        this.attrs = attrs;
    }
}
