package com.anthonydenaud.rconark.network;

import android.content.Context;

import com.anthonydenaud.rconark.exception.PacketParseException;
import com.google.inject.Inject;

import com.anthonydenaud.rconark.R;
import com.anthonydenaud.rconark.event.ConnectionListener;
import com.anthonydenaud.rconark.event.OnReceiveListener;
import com.anthonydenaud.rconark.event.ReceiveEvent;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class SRPConnection {

    private final Context context;
    private int sequenceNumber;

    private Thread connectionThread;
    private Thread receiveThread;

    private Socket client;
    private final LinkedHashMap<Integer, Packet> outgoingPackets;

    private boolean runReceiveThread;
    private boolean isConnected;

    private OnReceiveListener onReceiveListener;
    private ConnectionListener connectionListener;
    private Date lastPacketTime;


    @Inject
    public SRPConnection(Context context) {
        this.context = context;
        outgoingPackets = new LinkedHashMap<>();
    }


    public void open(final String hostname, final int port) {
        lastPacketTime = new Date();
        if (!isConnected) {
            client = new Socket();
            connectionThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        isConnected = true;
                        client.connect(new InetSocketAddress(hostname, port));
                        connectionListener.onConnect();
                        runReceiveThread = true;
                        beginReceive();

                    } catch (IOException e) {
                        isConnected = false;
                        if (connectionListener != null) {
                            connectionListener.onConnectionFail(context.getString(R.string.connection_fail));
                        }
                    }
                }
            });
            connectionThread.setName("ConnectionThread");
            connectionThread.start();
        }
    }

    public synchronized int getSequenceNumber() {
        return ++sequenceNumber;
    }


    public void send(final Packet packet) throws IOException {

        synchronized (outgoingPackets) {
            this.outgoingPackets.put(packet.getId(), packet);
        }


        if (client != null && client.isConnected()) {
            byte[] data = packet.encode();
            OutputStream outputStream = client.getOutputStream();
            outputStream.write(data);
        } else {
            isConnected = false;
            if (connectionListener != null) {
                connectionListener.onDisconnect();
            }
        }

    }

    private void beginReceive() {
        receiveThread = new Thread(new Runnable() {
            @Override
            public void run() {
                receive();
            }
        });
        receiveThread.setName("ReceiverThread");
        receiveThread.start();
    }

    private void receive() {
        if (runReceiveThread) {
            InputStream inputStream;
            try {
/*
                if (new Date().getTime() - lastPacketTime.getTime() > 3000) {
                    close();
                }
*/
                byte[] response = new byte[Packet.PACKET_MAX_LENGTH];
                inputStream = client.getInputStream();
                int totalRead = inputStream.read(response, 0, response.length);

                try {
                    if (totalRead > 0) {
                        Packet packet = new Packet(response);
                        if ((packet.getId() == -1 || packet.getId() > 0) && onReceiveListener != null) {
                            lastPacketTime = new Date();
                            onReceiveListener.onReceive(new ReceiveEvent(SRPConnection.this, packet));
                        }
                    }
                } catch (PacketParseException e) {
                    e.printStackTrace();
                    synchronized (outgoingPackets) {
                        send(outgoingPackets.get(outgoingPackets.size()));
                    }
                }
                receive();
            } catch (IOException e) {
                connectionListener.onDisconnect();
            }
        }
    }

    public void setOnReceiveListener(OnReceiveListener onReceiveListener) {
        this.onReceiveListener = onReceiveListener;
    }

    public void setConnectionListener(ConnectionListener connectionListener) {
        this.connectionListener = connectionListener;
    }

    public void close() throws IOException {
        if (client != null) {
            client.close();
            isConnected = false;
            runReceiveThread = false;
            receiveThread.interrupt();
            connectionThread.interrupt();
        }
    }


    public boolean isConnected() {
        return client.isConnected();
    }

    public synchronized Packet getRequestPacket(int id) {
        return outgoingPackets.get(id);
    }
}
