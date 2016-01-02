package com.anthonydenaud.rconark.network;

import android.content.Context;

import com.anthonydenaud.rconark.event.OnServerStopRespondingListener;
import com.anthonydenaud.rconark.exception.PacketParseException;
import com.google.inject.Inject;

import com.anthonydenaud.rconark.R;
import com.anthonydenaud.rconark.event.ConnectionListener;
import com.anthonydenaud.rconark.event.OnReceiveListener;
import com.anthonydenaud.rconark.event.ReceiveEvent;

import org.apache.commons.lang3.StringUtils;

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

import roboguice.util.Ln;


public class SRPConnection {

    private final Context context;
    private int sequenceNumber;

    private Thread connectionThread;
    private Thread receiveThread;

    private Socket client;
    private final LinkedHashMap<Integer, Packet> outgoingPackets;
    private final LinkedHashMap<Integer, Packet> sendQueue;

    private boolean runReceiveThread;
    private boolean isConnected;

    private OnReceiveListener onReceiveListener;
    private ConnectionListener connectionListener;
    private OnServerStopRespondingListener onServerStopRespondingListener;
    private boolean reconnecting = false;

    public void setOnServerStopRespondingListener(OnServerStopRespondingListener onServerStopRespondingListener) {
        this.onServerStopRespondingListener = onServerStopRespondingListener;
    }

    private Date lastPacketTime;


    @Inject
    public SRPConnection(Context context) {
        this.context = context;
        outgoingPackets = new LinkedHashMap<>();
        sendQueue = new LinkedHashMap<>();
    }


    public void open(final String hostname, final int port) {
        Ln.d("Connecting to %s:%d ...",hostname,port);

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

        if (StringUtils.isNotEmpty(packet.getBody())) {
            Ln.d("Send : %s", packet.getBody());
        }


        synchronized (outgoingPackets) {
            this.outgoingPackets.put(packet.getId(), packet);
        }

        synchronized (sendQueue) {
            sendQueue.put(packet.getId(), packet);

            int key = sendQueue.entrySet().iterator().next().getKey();

            if (client != null && client.isConnected()) {
                byte[] data = sendQueue.get(key).encode();
                OutputStream outputStream = client.getOutputStream();
                outputStream.write(data);
            } else {
                isConnected = false;
                if (connectionListener != null) {
                    Ln.e("Unable to send packet : connection closed.");
                   // connectionListener.onDisconnect();
                }
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

                if (new Date().getTime() - lastPacketTime.getTime() > 3000) {
                    reconnect();
                }

                byte[] response = new byte[Packet.PACKET_MAX_LENGTH];
                inputStream = client.getInputStream();
                int totalRead = inputStream.read(response, 0, response.length);

                try {
                    if (totalRead > 0) {
                        Packet packet = new Packet(response);
                        if ((packet.getId() == -1 || packet.getId() > 0) && onReceiveListener != null) {

                            synchronized (sendQueue) {
                                sendQueue.remove(packet.getId());
                            }

                            lastPacketTime = new Date();
                            onReceiveListener.onReceive(new ReceiveEvent(SRPConnection.this, packet));

                            if(StringUtils.isNotEmpty(packet.getBody())){
                                Ln.d("Receive : %s",packet.getBody());
                            }
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
                Ln.e("Unable to send packet : %s", e.getMessage());
            }
        }
    }

    private void reconnect() {

        reconnecting = true;

        try {
            close();

        } catch (IOException e) {
            Ln.e("Unable to close client : %s", e.getLocalizedMessage());
        }

        Ln.e("The server has stopped to responding to RCON requests.");
        Ln.e("Reconnecting ...");
        if(onServerStopRespondingListener !=null){
            onServerStopRespondingListener.onServerStopResponding();
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
        sendQueue.clear();
        outgoingPackets.clear();
    }


    public boolean isConnected() {
        return client.isConnected();
    }

    public synchronized Packet getRequestPacket(int id) {
        return outgoingPackets.get(id);
    }

    public boolean isReconnecting() {
        return reconnecting;
    }

    public void setReconnecting(boolean reconnecting) {
        this.reconnecting = reconnecting;
    }
}
