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
     * 返回虚拟节点ID
     * 它由两部分组成：NodeId + VirtualId。在可以唯一标识一个虚拟设备。
     *
     * @return 虚拟节点ID
     */
    String virtualNodeId();

    /**
     * 返回返回消息Id
     *
     * @return 返回消息Id
     */
    long eventId();

    ////

    final class Header {

        public final byte magic;
        public final byte version;
        public final byte controlVar;
        public final long eventId;

        public Header(byte magic, byte version, byte controlVar, long eventId) {
            this.magic = magic;
            this.version = version;
            this.controlVar = controlVar;
            this.eventId = eventId;
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
                dos.writeLong(header.eventId);
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
        public String virtualNodeId() {
            return this.sourceNodeId;
        }

        @Override
        public long eventId() {
            return this.header.eventId;
        }

    }

    ////

    static String makeVirtualNodeId(String nodeId, String virtualId) {
        return nodeId + ":" + virtualId;
    }

    /**
     * 根据字节数据，创建Message对象
     *
     * @param virtualNodeId Virtual Node Id
     * @param body          body
     * @param eventId         EventID
     * @return Message
     */
    static Message newMessageById(String virtualNodeId, byte[] body, long eventId) {
        return new message(
                new Header(FrameMagic, FrameVersion, FrameVarData, eventId),
                virtualNodeId,
                body);
    }

    /**
     * 根据字节数据，创建Message对象
     *
     * @param nodeId    Node Id
     * @param virtualId Virtual Id
     * @param body      body
     * @param eventId     EventID
     * @return Message
     */
    static Message newMessageWith(String nodeId, String virtualId, byte[] body, long eventId) {
        return newMessageById(
                makeVirtualNodeId(nodeId, virtualId),
                body,
                eventId
        );
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
        // HeaderSize: (Magic + Ver + Var) + Long(8)
        final int headerSize = 3 + 8;
        try {
            final byte magic = dis.readByte();
            final byte version = dis.readByte();
            final byte vars = dis.readByte();
            final long eventId = dis.readLong();
            final byte[] name = read0(data, headerSize, FrameEmpty, true);
            final byte[] body = read0(data, (headerSize + 1) + name.length, FrameEmpty, false);
            return new message(new Header(
                    magic,
                    version,
                    vars,
                    eventId
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
