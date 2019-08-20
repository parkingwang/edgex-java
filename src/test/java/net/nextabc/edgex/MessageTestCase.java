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
        Message msg = Message.newMessage("CHEN", "NODE", "A", null, body, 2019);
        check(msg);
        Message passed = Message.parse(msg.bytes());
        check(passed);
    }

    private void check(Message msg) {
        Assert.assertEquals(Message.FrameMagic, msg.header().magic);
        Assert.assertEquals(Message.FrameVersion, msg.header().version);
        Assert.assertEquals(Message.FrameVarData, msg.header().controlVar);
        Assert.assertEquals(2019, msg.eventId());
        Assert.assertEquals(Message.makeUnionId("CHEN", "NODE", "A", null), msg.unionId());
        Assert.assertArrayEquals(body, msg.body());
    }

    @Test
    public void testSplitUnionId_1() {
        final String[] uid_1 = Message.splitUnionId("");
        check(uid_1, 0, "");
        check(uid_1, 1, "");
        check(uid_1, 2, "");
        check(uid_1, 3, "");

        final String[] uid_2 = Message.splitUnionId("a");
        check(uid_2, 0, "a");
        check(uid_2, 1, "");
        check(uid_2, 2, "");
        check(uid_2, 3, "");

        final String[] uid_3 = Message.splitUnionId("a:");
        check(uid_3, 0, "a");
        check(uid_3, 1, "");
        check(uid_3, 2, "");
        check(uid_3, 3, "");

        final String[] uid_4 = Message.splitUnionId("a:b");
        check(uid_4, 0, "a");
        check(uid_4, 1, "b");
        check(uid_4, 2, "");
        check(uid_4, 3, "");

        final String[] uid_5 = Message.splitUnionId("a::c");
        check(uid_5, 0, "a");
        check(uid_5, 1, "");
        check(uid_5, 2, "c");
        check(uid_5, 3, "");

        final String[] uid_6 = Message.splitUnionId("a:b:c::e:f");
        check(uid_6, 0, "a");
        check(uid_6, 1, "b");
        check(uid_6, 2, "c");
        check(uid_6, 3, "");

        final String[] uid_7 = Message.splitUnionId("a:b:c:d:e:f");
        check(uid_7, 0, "a");
        check(uid_7, 1, "b");
        check(uid_7, 2, "c");
        check(uid_7, 3, "d");
    }

    private void check(String[] uid, int idx, String excepted) {
        Assert.assertEquals(4, uid.length);
        Assert.assertEquals(excepted, uid[idx]);
    }
}
