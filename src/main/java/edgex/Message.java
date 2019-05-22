package edgex;

import org.apache.log4j.Logger;

/**
 * @author 陈永佳 (yoojiachen@gmail.com)
 * @version 0.0.1
 */
public interface Message {

    byte FRAME_VAR_BITS = (byte) 0xED;
    byte FRAME_HEADER_SIZE = 2;
    byte FRAME_NAME_MAX_SIZE = Byte.MAX_VALUE;

    /**
     * 返回消息全部字节
     *
     * @return 返回消息全部字节
     */
    byte[] getFrames();

    /**
     * 返回消息的Header
     *
     * @return 返回消息的Header
     */
    Header header();

    /**
     * 回消息体字节
     *
     * @return 回消息体字节
     */
    byte[] body();

    /**
     * 返回消息体创建源组件名字
     *
     * @return 返回消息体创建源组件名字
     */
    byte[] name();

    /**
     * 返回Body消息体的大小
     *
     * @return 返回Body消息体的大小
     */
    int size();

    ////

    final class Header {
        public final byte varBits;
        public final byte nameLen;

        private Header(byte varBits, byte nameLen) {
            this.varBits = varBits;
            this.nameLen = nameLen;
        }
    }

    ////

    final class Impl implements Message {

        private final byte[] head;
        private final byte[] name;
        private final byte[] body;

        private Impl(byte[] head, byte[] name, byte[] body) {
            this.head = head;
            this.name = name;
            this.body = body;
        }

        @Override
        public byte[] getFrames() {
            final byte[] out = new byte[head.length + name.length + body.length];
            int offset = 0;
            System.arraycopy(head, 0, out, offset, head.length);
            offset += head.length;
            System.arraycopy(name, 0, out, offset, name.length);
            offset += name.length;
            System.arraycopy(body, 0, out, offset, body.length);
            return out;

        }

        @Override
        public Header header() {
            return new Header(
                    this.head[0],
                    this.head[1]
            );
        }

        @Override
        public byte[] body() {
            return this.body;
        }

        @Override
        public byte[] name() {
            return this.name;
        }

        @Override
        public int size() {
            return body.length;
        }
    }

    ////

    /**
     * 根据字符数据，创建Message对象
     *
     * @param name 　Name
     * @param body Body
     * @return Message
     */
    static Message fromString(String name, String body) {
        return fromBytes(name.getBytes(), body.getBytes());
    }

    /**
     * 根据字节数据，创建Message对象
     *
     * @param name Name
     * @param body Body
     * @return Message
     */
    static Message fromBytes(byte[] name, byte[] body) {
        if (name.length > FRAME_NAME_MAX_SIZE) {
            Logger.getLogger("MessageBuilder").fatal("Name len too large: " + name.length);
        }
        return new Impl(new byte[]{
                FRAME_VAR_BITS,
                (byte) name.length,
        }, name, body);
    }

    /**
     * 解析字节码数据为Message对象
     *
     * @param data 　数据
     * @return Message
     */
    static Message parse(byte[] data) {
        int offset = 0;

        // head
        final byte[] head = new byte[FRAME_HEADER_SIZE];
        System.arraycopy(data, offset, head, 0, head.length);
        final Header header = new Header(
                head[0],
                head[1]
        );

        // name
        offset += head.length;
        final byte[] name = new byte[header.nameLen];
        System.arraycopy(data, offset, name, 0, name.length);

        // body
        offset += name.length;
        final byte[] body = new byte[data.length - offset];
        System.arraycopy(data, offset, body, 0, body.length);

        return new Impl(head, name, body);
    }
}
