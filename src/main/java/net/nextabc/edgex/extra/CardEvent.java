package net.nextabc.edgex.extra;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Data;

/**
 * 刷卡事件数据实体
 *
 * @author 陈哈哈 (yoojiachen@gmail.com)
 */
@Data
@Builder
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
    private final int serialNum;

    /**
     * 控制主板ID
     */
    @SerializedName("boardId")
    private final int boardId;

    /**
     * 门号
     */
    @SerializedName("doorId")
    private final byte doorId;

    /**
     * 进出方向
     */
    @SerializedName("direct")
    private final byte direct;

    /**
     * 卡号
     */
    @SerializedName("card")
    private final String cardNO;

    /**
     * 事件类型
     */
    @SerializedName("type")
    private final String type;

    /**
     * 状态
     */
    @SerializedName("state")
    private final String state;

    /**
     * 内部流水号
     */
    @SerializedName("index")
    private final int index;

    public CardEvent(int serialNum, int boardId, byte doorId, byte direct, String cardNO, String type, String state, int index) {
        this.serialNum = serialNum;
        this.boardId = boardId;
        this.doorId = doorId;
        this.direct = direct;
        this.cardNO = cardNO;
        this.type = type;
        this.state = state;
        this.index = index;
    }

    ////

    /**
     * 返回进出方向名称
     */
    public static String directName(byte direct) {
        if (directIn == direct) {
            return "IN";
        } else {
            return "OUT";
        }
    }
}