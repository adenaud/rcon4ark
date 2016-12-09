package com.anthonydenaud.arkrcon.network;

import android.content.Context;

import com.anthonydenaud.arkrcon.event.OnServerStopRespondingListener;
import com.anthonydenaud.arkrcon.RavenLogger;
import com.google.inject.Inject;

import com.anthonydenaud.arkrcon.R;
import com.anthonydenaud.arkrcon.event.ConnectionListener;
import com.anthonydenaud.arkrcon.event.OnReceiveListener;
import com.anthonydenaud.arkrcon.event.ReceiveEvent;

import org.apache.commons.collections4.map.LinkedMap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Date;

import roboguice.util.Ln;

public class SRPConnection {

    private final Context context;
    private int sequenceNumber;

    private Thread connectionThread;
    private Thread receiveThread;

    private Socket client;
    private final LinkedMap<Integer, Packet> outgoingPackets;

    private boolean runReceiveThread;
    private boolean isConnected;

    private OnReceiveListener onReceiveListener;
    private ConnectionListener connectionListener;
    private OnServerStopRespondingListener onServerStopRespondingListener;
    private boolean reconnecting = false;

    private Date lastPacketTime;

    @Inject
    public SRPConnection(Context context) {
        this.context = context;
        outgoingPackets = new LinkedMap<>();
    }


    public void open(final String hostname, final int port) {
        Ln.d("Connecting to %s:%d ...", hostname, port);

        lastPacketTime = new Date();
        if (!isConnected) {
            client = new Socket();
            connectionThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        isConnected = true;
                        client.connect(new InetSocketAddress(hostname, port));
                        connectionListener.onConnect(reconnecting);
                        runReceiveThread = true;
                        reconnecting = false;
                        Ln.d("Connected");
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

    public void send(final Packet packet) {

        synchronized (outgoingPackets) {
            this.outgoingPackets.put(packet.getId(), packet);
        }

        if (client != null && client.isConnected()) {

            Thread sendThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    byte[] data = new byte[0];
                    try {
                        data = packet.encode();
                        OutputStream outputStream = client.getOutputStream();
                        outputStream.write(data);
                    } catch (IOException e) {
                        connectionListener.onConnectionDrop();
                    }
                }
            });
            sendThread.setName("SendThread");
            sendThread.start();


        } else {
            isConnected = false;
            Ln.w("Unable to send packet : connection closed.");
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
        while (runReceiveThread) {
            InputStream inputStream;
            try {

                if (new Date().getTime() - lastPacketTime.getTime() > 3000) {
                    reconnect();
                }
                inputStream = client.getInputStream();
                byte[] packetSize = new byte[4];
                int packetSizeInt = 0;
                int sizeLength = inputStream.read(packetSize, 0, packetSize.length);
                if (sizeLength == 4 && !PacketUtils.isText(packetSize)) {
                    packetSizeInt = PacketUtils.getPacketSize(packetSize) + 10;
                }

                final byte[] response;
                if (!PacketUtils.isText(packetSize)) {
                    response = new byte[packetSizeInt];
                } else {
                    response = new byte[Packet.PACKET_MAX_LENGTH];
                }

                int responseLength = inputStream.read(response, 0, response.length);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byteArrayOutputStream.write(packetSize);
                byteArrayOutputStream.write(response);
                final byte[] packetBuffer = byteArrayOutputStream.toByteArray();


                if (responseLength > 0) {

                    if (PacketUtils.isStartPacket(packetBuffer)) {
                        final Packet packet = new Packet(packetBuffer);
                        if ((packet.getId() == -1 || packet.getId() > 0) && onReceiveListener != null) {
                            lastPacketTime = new Date();

                            Thread thread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    onReceiveListener.onReceive(new ReceiveEvent(SRPConnection.this, packet));
                                }
                            }, "ResponseExecThread");
                            thread.start();
                        }
                    } else {
                        final Packet lastPacket = outgoingPackets.get(outgoingPackets.lastKey());

                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                if (lastPacket.getBody().equals("getgamelog")) {
                                    Packet packet = new Packet(lastPacket.getId(), PacketType.SERVERDATA_RESPONSE_VALUE.getValue(), new String(packetBuffer));
                                    onReceiveListener.onReceive(new ReceiveEvent(SRPConnection.this, packet));
                                } else if (lastPacket.getBody().equals("ListPlayers")) {
                                    Packet packet = new Packet(getSequenceNumber(), PacketType.SERVERDATA_EXECCOMMAND.getValue(), "ListPlayers");
                                    send(packet);
                                }
                            }
                        }, "ResponseExecThread");
                        thread.start();
                    }
                }
            } catch (IOException e) {
                Ln.w("Unable to receive packet : %s", e.getMessage());
                RavenLogger.getInstance().warn(SRPConnection.class, "Unable to receive packet : " + e.getMessage(), e);
                runReceiveThread = false;
            }
        }
    }

    public void reconnect() {
        if (!reconnecting) {
            reconnecting = true;
            try {
                close();
            } catch (IOException e) {
                Ln.e("Unable to close client : %s", e.getLocalizedMessage());
                RavenLogger.getInstance().error(SRPConnection.class,"Unable to close client :  "+ e.getLocalizedMessage(), e);
            }
            Ln.w("The server has stopped to responding to RCON requests, Reconnecting ...");
            if (onServerStopRespondingListener != null) {
                onServerStopRespondingListener.onServerStopResponding();
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
        outgoingPackets.clear();
    }

    public boolean isConnected() {
        return client.isConnected();
    }

    public synchronized Packet getRequestPacket(int id) {
        return outgoingPackets.get((Integer) id);
    }

    public boolean isReconnecting() {
        return reconnecting;
    }

    public void setReconnecting(boolean reconnecting) {
        this.reconnecting = reconnecting;
    }

    public void setOnServerStopRespondingListener(OnServerStopRespondingListener onServerStopRespondingListener) {
        this.onServerStopRespondingListener = onServerStopRespondingListener;
    }
}
