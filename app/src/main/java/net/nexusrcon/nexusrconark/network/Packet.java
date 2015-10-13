package net.nexusrcon.nexusrconark.network;


import org.apache.commons.lang3.ArrayUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by Anthony on 09/10/2015.
 */
public class Packet {

    private int size;
    private int id;
    private int type;
    private String body;

    public Packet() {

    }

    public Packet(int type, String body) {
        this.type = type;
        this.body = body;
    }

    public Packet(byte[] rawPacket) {
        decode(rawPacket);
    }

    public byte[] encode() {
        ByteArrayOutputStream packetOutput = new ByteArrayOutputStream();

        try {
            packetOutput.write(getUint32Bytes( body.length() + 10));
            packetOutput.write(getUint32Bytes(id));
            packetOutput.write(getUint32Bytes(type));
            packetOutput.write((body + '\0').getBytes());
            packetOutput.write(0x00);
        } catch (IOException e) {
            e.printStackTrace();
        }


        return packetOutput.toByteArray();
    }

    public Packet decode(byte[] rawPacket) {

        size = getIntFromBytes(rawPacket,0);
        id = getIntFromBytes(rawPacket,4);
        type = getIntFromBytes(rawPacket,8);
        body = getStringFromBytes(rawPacket,12,size - 9);
        return this;
    }

    private int getIntFromBytes(byte[] data, int index){
        byte[] res = ByteBuffer.allocate(4).array();
        res = Arrays.copyOfRange(data, index, index + 4);
        ArrayUtils.reverse(res);
        int integer = (res[0] << 24) & 0xff000000 | (res[1] << 16) & 0x00ff0000 | (res[2] << 8) & 0x0000ff00 | (res[3] << 0) & 0x000000ff;
        return  integer;
    }

    private String getStringFromBytes(byte[] data, int index, int length){
        byte[] res = new byte[] {};
        res = Arrays.copyOfRange(data, index, index + length -1);
        return new String(res);
    }

    private byte[] getUint32Bytes(final int value) {
        byte[] result = ByteBuffer.allocate(4).putInt(value).array();
        ArrayUtils.reverse(result);
        return result;
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
