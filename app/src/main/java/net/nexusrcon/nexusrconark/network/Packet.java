package net.nexusrcon.nexusrconark.network;

/**
 * Created by Anthony on 09/10/2015.
 */
public class Packet {

    private int size;
    private int id;
    private int type;
    private String body;


    public Packet(int size, int id, int type, String body) {
        this.size = size;
        this.id = id;
        this.type = type;
        this.body = body;
    }
}
