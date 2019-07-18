package net.nextabc.edgex;

import java.io.*;

/**
 * @author 陈永佳 (yoojiachen@gmail.com)
 * @version 0.0.1
 */
public interface Message {

    byte FrameMagic = (byte) 0xED; // Magic
    byte FrameVersion = 0x01; // 版本
    byte FrameEmpty = 0x00; // 分隔空帧
    byte FrameVarData = (byte) 0xDA;
    byte FrameVarPing = (byte) 0xD0;
    byte FrameVarPong = (byte) 0xD1;

    /**
     * 返回消息全部字节
     *
     * @return 返回消息全部字节
     */
    byte[] bytes();

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
    String sourceNodeId();

    /**
     * 返回返回消息Id
     *
     * @return 返回消息Id
     */
    int sequenceId();

    ////

    final class Header {

        public final byte magic;
        public final byte version;
        public final byte controlVar;
        public final int sequenceId;

        public Header(byte magic, byte version, byte controlVar, int sequenceId) {
            this.magic = magic;
            this.version = version;
            this.controlVar = controlVar;
            this.sequenceId = sequenceId;
        }
    }

    ////

    final class message implements Message {

        private final Header header;
        private final String sourceNodeId;
        private final byte[] body;

        private message(Header header, String sourceNodeId, byte[] body) {
            this.header = header;
            this.sourceNodeId = sourceNodeId;
            this.body = body;
        }

        @Override
        public byte[] bytes() {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final DataOutputStream dos = new DataOutputStream(baos);
            try {
                dos.writeByte(header.magic);
                dos.writeByte(header.version);
                dos.writeByte(header.controlVar);
                dos.writeInt(header.sequenceId);
                dos.write(sourceNodeId.getBytes());
                dos.writeByte(FrameEmpty);
                dos.write(body);
                dos.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return baos.toByteArray();
        }

        @Override
        public Header header() {
            return header;
        }

        @Override
        public byte[] body() {
            return this.body;
        }

        @Override
        public String sourceNodeId() {
            return this.sourceNodeId;
        }

        @Override
        public int sequenceId() {
            return this.header.sequenceId;
        }

    }

    ////

    static String makeSourceNodeId(String nodeName, String virtualNodeId) {
        return nodeName + ":" + virtualNodeId;
    }

    /**
     * 根据字符数据，创建Message对象
     *
     * @param nodeName      NodeName
     * @param virtualNodeId Virtual Node ID
     * @param body          Body
     * @param seqId         流水ID
     * @return Message
     */
    static Message fromString(String nodeName, String virtualNodeId, String body, int seqId) {
        return fromBytes(nodeName, virtualNodeId, body.getBytes(), seqId);
    }

    /**
     * 根据字节数据，创建Message对象
     *
     * @param nodeName      NodeName
     * @param virtualNodeId Virtual Node ID
     * @param body          Body
     * @param seqId         流水ID
     * @return Message
     */
    static Message fromBytes(String nodeName, String virtualNodeId, byte[] body, int seqId) {
        return create(makeSourceNodeId(nodeName, virtualNodeId), body, FrameVarData, seqId);
    }

    /**
     * 根据字节数据，创建Message对象
     *
     * @param sourceNodeId SourceName
     * @param body         Body
     * @param ctrlVar      Control Var
     * @param seqId        流水ID
     * @return Message
     */
    static Message create(String sourceNodeId, byte[] body, byte ctrlVar, int seqId) {
        return new message(new Header(
                FrameMagic,
                FrameVersion,
                ctrlVar,
                seqId), sourceNodeId, body);
    }

    /**
     * 解析字节码数据为Message对象
     *
     * @param data 　数据
     * @return Message
     */
    static Message parse(byte[] data) {
        final ByteArrayInputStream bais = new ByteArrayInputStream(data);
        final DataInputStream dis = new DataInputStream(bais);
        try {
            final byte magic = dis.readByte();
            final byte version = dis.readByte();
            final byte vars = dis.readByte();
            final int seqId = dis.readInt();
            // header size: 7
            final byte[] name = read0(data, 7, FrameEmpty, true);
            final byte[] body = read0(data, 8 + name.length, FrameEmpty, false);
            return new message(new Header(
                    magic,
                    version,
                    vars,
                    seqId
            ), new String(name), body);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static byte[] read0(byte[] data, int offset, byte delimiter, boolean needDelimiter) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (int i = offset; i < data.length; i++) {
            if (needDelimiter && data[i] == delimiter) {
                break;
            } else {
                out.write(data[i]);
            }
        }
        return out.toByteArray();
    }
}
