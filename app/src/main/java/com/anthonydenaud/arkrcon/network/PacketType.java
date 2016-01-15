package com.anthonydenaud.arkrcon.network;

/**
 * Created by Anthony on 13/10/2015.
 */
public enum PacketType {
    SERVERDATA_AUTH(3),SERVERDATA_EXECCOMMAND(2),SERVERDATA_AUTH_RESPONSE(2),SERVERDATA_RESPONSE_VALUE(0);

    private int value;

    PacketType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
