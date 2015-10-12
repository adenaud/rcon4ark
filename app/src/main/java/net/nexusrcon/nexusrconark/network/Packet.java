package net.nexusrcon.nexusrconark.network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

    public Packet(int id, int type, String body) {
        this.size = body.length() + 9;
        this.id = id;
        this.type = type;
        this.body = body;
    }

    public Packet(byte[] rawPacket) {
        decode(rawPacket);
    }

    public byte[] encode() {
        byte[] packet = null;


        ByteArrayOutputStream packetOutput = new ByteArrayOutputStream();

        try {
            packetOutput.write(size);
            packetOutput.write(id);
            packetOutput.write(type);
            packetOutput.write((body + '\0').getBytes());
            packetOutput.write(0);
        } catch (IOException e) {
            e.printStackTrace();
        }


        return packet;
    }

    public Packet decode(byte[] rawPacket) {

        size = getIntFromBytes(rawPacket,0);
        id = getIntFromBytes(rawPacket,4);
        type = getIntFromBytes(rawPacket,8);
        body = getStringFromBytes(rawPacket,12,size - 9);

        return this;
    }

    private int getIntFromBytes(byte[] data, int index){
        byte[] res;
        res = Arrays.copyOfRange(data,index, index + 4);
        int integer = (res[0] << 24) & 0xff000000 | (res[1] << 16) & 0x00ff0000 | (res[2] << 8) & 0x0000ff00 | (res[3] << 0) & 0x000000ff;
        return  integer;
    }

    private String getStringFromBytes(byte[] data, int index, int length){
        byte[] res = new byte[] {};
        res = Arrays.copyOfRange(data, index, index + length + 1);
        return "";
    }

}
