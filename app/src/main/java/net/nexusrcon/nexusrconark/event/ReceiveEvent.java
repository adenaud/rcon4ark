package net.nexusrcon.nexusrconark.event;

import net.nexusrcon.nexusrconark.network.Packet;
import net.nexusrcon.nexusrconark.network.SRPConnection;

public class ReceiveEvent {
	private SRPConnection connection;
	private Packet packet;

	public ReceiveEvent(SRPConnection connection, Packet packet) {
		super();
		this.connection = connection;
		this.packet = packet;
	}
	public SRPConnection getConnection() {
		return connection;
	}
	public Packet getPacket() {
		return packet;
	}
}
