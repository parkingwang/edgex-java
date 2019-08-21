package net.nextabc.edgex.internal;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * 虚拟节点状态实体
 *
 * @author 陈哈哈 (yoojiachen@gmail.com)
 */
@Data
@Builder
public class VirtualNodeState {

    @SerializedName("unionId")
    private final String unionId;

    @SerializedName("nodeId")
    private final String nodeId;

    @SerializedName("boardId")
    private final String boardId;

    @SerializedName("majorId")
    private final String majorId;

    @SerializedName("minorId")
    private final String minorId;

    @SerializedName("state")
    private final String state;

    @SerializedName("values")
    private final Map<String, Object> values;

    public VirtualNodeState(String unionId, String nodeId, String boardId, String majorId, String minorId,
                            String state, Map<String, Object> values) {
        this.unionId = unionId;
        this.nodeId = nodeId;
        this.boardId = boardId;
        this.majorId = majorId;
        this.minorId = minorId;
        this.state = state;
        this.values = values;
    }
}
