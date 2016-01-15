package com.anthonydenaud.arkrcon.event;

import com.anthonydenaud.arkrcon.network.Packet;
import com.anthonydenaud.arkrcon.network.SRPConnection;

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
