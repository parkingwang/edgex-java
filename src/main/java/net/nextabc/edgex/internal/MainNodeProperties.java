package net.nextabc.edgex.internal;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author 陈永佳 (yoojiachen@gmail.com)
 * @version 0.0.1
 */
@Data
@Builder
public class MainNodeProperties {

    @SerializedName("hostOS")
    private final String hostOS;

    @SerializedName("hostArch")
    private final String hostArch;

    @SerializedName("nodeType")
    private final String nodeType;

    @SerializedName("nodeId")
    private final String nodeId;

    @SerializedName("vendor")
    private final String vendor;

    @SerializedName("connDriver")
    private final String connDriver;

    @SerializedName("nodes")
    private List<VirtualNodeProperties> nodes;

    public MainNodeProperties(String hostOS, String hostArch, String nodeType, String nodeId,
                              String vendor, String connDriver,
                              List<VirtualNodeProperties> nodes) {
        this.hostOS = hostOS;
        this.hostArch = hostArch;
        this.nodeType = nodeType;
        this.nodeId = nodeId;
        this.vendor = vendor;
        this.connDriver = connDriver;
        this.nodes = nodes;
    }

}
