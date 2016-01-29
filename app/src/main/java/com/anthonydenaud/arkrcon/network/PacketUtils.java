package com.anthonydenaud.arkrcon.network;

import com.anthonydenaud.arkrcon.exception.PacketParseException;

import org.apache.commons.lang3.ArrayUtils;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import roboguice.util.Ln;

public class PacketUtils {


    public static int getIntFromBytes(byte[] data, int index) {
        byte[] res;
        res = Arrays.copyOfRange(data, index, index + 4);
        ArrayUtils.reverse(res);
        return (res[0] << 24) & 0xff000000 | (res[1] << 16) & 0x00ff0000 | (res[2] << 8) & 0x0000ff00 | (res[3]) & 0x000000ff;
    }

    public static String getStringFromBytes(byte[] data, int index, int length) throws PacketParseException {
        String string = "";
        byte[] res;
        try {
            if (length > Packet.PACKET_MAX_LENGTH) {
                throw new PacketParseException("Error bad length ( > "+Packet.PACKET_MAX_LENGTH+" ) : " + length + " raw data : " + new String(data));
            }
            res = Arrays.copyOfRange(data, index, index + length - 1);
            string = new String(res, "US-ASCII");
        } catch (IllegalArgumentException | UnsupportedEncodingException e) {
            res = "".getBytes();
            Ln.e("getStringFromBytes -> IllegalArgumentException : " + res.toString());
        }
        return string;
    }

    public static byte[] getUint32Bytes(final int value) {
        byte[] result = ByteBuffer.allocate(4).putInt(value).array();
        ArrayUtils.reverse(result);
        return result;
    }

    public static int getPacketSize(byte[] byteBuffer){
        int size;
        size = getIntFromBytes(byteBuffer,0);
        return size;
    }

    public static boolean isStartPacket(byte[] byteBuffer){
        boolean isStartPacket = false;
        int type = PacketUtils.getIntFromBytes(byteBuffer, 8);

        if((!isText(byteBuffer)) && (type >= 0 && type <= 3)){
            isStartPacket = true;
        }

        return isStartPacket;
    }

    public static boolean isText(byte[] byteBuffer){
        byte[] data = Arrays.copyOfRange(byteBuffer, 0, 4);
        boolean isText = false;
        int charByteNumber = 0;
        for (int i=0; i<data.length; i++){
            if(data[i] > 20){
                charByteNumber++;
            }
        }
        if(charByteNumber == 4){
            isText = true;
        }
        return isText;
    }

}
