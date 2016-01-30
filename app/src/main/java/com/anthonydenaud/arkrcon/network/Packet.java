package com.anthonydenaud.arkrcon.network;



import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Packet {

    public static int PACKET_MAX_LENGTH = 2097152; //2 MegaBytes

    private int size;
    private int id;
    private int type;
    private String body;

    public Packet() {

    }

    public Packet(int id, int type, String body) {
        this.id = id;
        this.type = type;
        this.body = body;
    }

    public Packet(byte[] rawPacket) {
        decode(rawPacket);
    }

    public byte[] encode() throws IOException {
        ByteArrayOutputStream packetOutput = new ByteArrayOutputStream();

        packetOutput.write(PacketUtils.getUint32Bytes(body.length() + 10));
        packetOutput.write(PacketUtils.getUint32Bytes(id));
        packetOutput.write(PacketUtils.getUint32Bytes(type));
        packetOutput.write((body + '\0').getBytes("US-ASCII"));
        packetOutput.write(0x00);

        return packetOutput.toByteArray();
    }

    public Packet decode(byte[] rawPacket) {

        size = PacketUtils.getIntFromBytes(rawPacket, 0);
        id = PacketUtils.getIntFromBytes(rawPacket, 4);
        type = PacketUtils.getIntFromBytes(rawPacket, 8);
        body = PacketUtils.getStringFromBytes(rawPacket, 12, size - 9);
        return this;
    }



    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
