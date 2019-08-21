package net.nextabc.edgex.extra;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * 虚拟节点属性数据实体
 * @author 陈永佳 (yoojiachen@gmail.com)
 * @version 0.0.1
 */
@Data
@Builder
public class VirtualNodeProperties {

    @SerializedName("unionId")
    private final String unionId;

    @SerializedName("groupId")
    private final String groupId;

    @SerializedName("majorId")
    private final String majorId;

    @SerializedName("minorId")
    private final String minorId;

    @SerializedName("description")
    private final String description;

    @SerializedName("virtual")
    private final boolean virtual;

    @SerializedName("stateCommands")
    private final Map<String, String> stateCommands;

    @SerializedName("attrs")
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
