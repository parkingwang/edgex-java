package net.nextabc.edgex.extra;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 陈哈哈 (yoojiachen@gmail.com)
 */
@Data
@Builder
@NoArgsConstructor
public class CardEvent {

    public static final byte directIn = 'I';
    public static final byte directOut = 'O';

    public static final String typeNop = "NOP";// 无操作
    public static final String typeCard = "CARD"; // 刷卡
    public static final String typeButton = "BUTTON"; // 开关事件
    public static final String typeOpen = "OPEN"; // 开门事件
    public static final String typeClose = "CLOSE"; // 关门事件
    public static final String typeAlarm = "ALARM"; // 报警事件

    /**
     * 控制序列号
     */
    @SerializedName("sn")
    private int serialNum;

    /**
     * 控制主板ID
     */
    @SerializedName("boardId")
    private int boardId;

    /**
     * 门号
     */
    @SerializedName("doorId")
    private byte doorId;

    /**
     * 进出方向
     */
    @SerializedName("direct")
    private byte direct;

    /**
     * 卡号
     */
    @SerializedName("card")
    private String cardNO;

    /**
     * 事件类型
     */
    @SerializedName("type")
    private String type;

    /**
     * 状态
     */
    @SerializedName("state")
    private String state;

    /**
     * 内部流水号
     */
    @SerializedName("index")
    private int index;
}