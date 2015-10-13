package net.nexusrcon.nexusrconark.event;

import net.nexusrcon.nexusrconark.network.Packet;

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
