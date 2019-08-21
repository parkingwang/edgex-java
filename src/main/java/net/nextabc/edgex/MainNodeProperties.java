package net.nextabc.edgex;

import lombok.Builder;
import lombok.Data;
import net.nextabc.edgex.extra.VirtualNodeProperties;

import java.util.List;

/**
 * @author 陈永佳 (yoojiachen@gmail.com)
 * @version 0.0.1
 */
@Data
@Builder
public class MainNodeProperties {

    private final String hostOS;
    private final String hostArch;
    private final String nodeType;
    private final String nodeId;
    private final String vendor;
    private final String connDriver;
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
