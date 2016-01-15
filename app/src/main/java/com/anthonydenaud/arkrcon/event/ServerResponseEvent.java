package com.anthonydenaud.arkrcon.event;

import com.anthonydenaud.arkrcon.network.Packet;

/**
 * Created by Anthony on 13/10/2015.
 */
public class ServerResponseEvent {

    private Packet packet;

    public ServerResponseEvent() {}

    public ServerResponseEvent(Packet packet) {
        this.packet = packet;
    }

    public Packet getPacket() {
        return packet;
    }

    public void setPacket(Packet packet) {
        this.packet = packet;
    }
}
