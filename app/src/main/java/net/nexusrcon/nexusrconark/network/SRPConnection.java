package net.nexusrcon.nexusrconark.network;

import android.content.Context;

import com.google.inject.Inject;

import net.nexusrcon.nexusrconark.R;
import net.nexusrcon.nexusrconark.event.ConnectionListener;
import net.nexusrcon.nexusrconark.event.OnReceiveListener;
import net.nexusrcon.nexusrconark.event.ReceiveEvent;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class SRPConnection {

    private final Context context;
    private int sequenceNumber;

    private Thread connectionThread;
    private Thread receiveThread;

    private Socket client;
    private Map<Integer, Packet> outgoingPackets;

    private boolean runReceiveThread;
    private boolean isConnected;

    private OnReceiveListener onReceiveListener;
    private ConnectionListener connectionListener;

    @Inject
    public SRPConnection(Context context) {
        this.context = context;
        outgoingPackets = new ConcurrentHashMap<>();
    }


    public void open(final String hostname, final int port) {

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
                        if(connectionListener != null){
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

        if (client != null &&client.isConnected()) {
            byte[] data = packet.encode();
            OutputStream outputStream = client.getOutputStream();
            outputStream.write(data);
            this.outgoingPackets.put(packet.getId(), packet);
        } else {
            isConnected = false;
            connectionListener.onDisconnect();

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

                byte[] response = new byte[4096];
                inputStream = client.getInputStream();
                inputStream.read(response, 0, response.length);

                Packet packet = new Packet(response);
                if ((packet.getId() == -1 || packet.getId() > 0) && onReceiveListener != null) {
                    onReceiveListener.onReceive(new ReceiveEvent(SRPConnection.this, packet));
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
        client.close();

        isConnected = false;
        runReceiveThread = false;

        receiveThread.interrupt();
        connectionThread.interrupt();
    }


    public boolean isConnected(){
        return client.isConnected();
    }

    public synchronized Packet getRequestPacket(int id) {
        return outgoingPackets.get(id);
    }
}
