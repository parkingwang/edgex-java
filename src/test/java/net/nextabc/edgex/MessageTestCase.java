package net.nextabc.edgex;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author 陈哈哈 (yoojiachen@gmail.com)
 */
public class MessageTestCase {

    private static final byte[] body = new byte[]{Message.FrameEmpty, (byte) 0xAA, Message.FrameEmpty, (byte) 0xBB, (byte) 0xCC, Message.FrameEmpty};

    @Test
    public void test() {
        Message msg = Message.fromBytes("CHEN", "NODE", body, 2019);
        check(msg);
        Message passed = Message.parse(msg.bytes());
        check(passed);
    }

    private void check(Message msg) {
        Assert.assertEquals(Message.FrameMagic, msg.header().magic);
        Assert.assertEquals(Message.FrameVersion, msg.header().version);
        Assert.assertEquals(Message.FrameVarData, msg.header().controlVar);
        Assert.assertEquals(2019, msg.sequenceId());
        Assert.assertEquals(Message.makeSourceNodeId("CHEN", "NODE"), msg.sourceNodeId());
        Assert.assertArrayEquals(body, msg.body());
    }
}
