package net.nexusrcon.nexusrconark;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import net.nexusrcon.nexusrconark.event.OnConnectListener;
import net.nexusrcon.nexusrconark.event.OnReceiveListener;
import net.nexusrcon.nexusrconark.event.ReceiveEvent;
import net.nexusrcon.nexusrconark.event.ServerResponseDispatcher;
import net.nexusrcon.nexusrconark.event.ServerResponseEvent;
import net.nexusrcon.nexusrconark.model.Server;
import net.nexusrcon.nexusrconark.network.Packet;
import net.nexusrcon.nexusrconark.network.PacketType;
import net.nexusrcon.nexusrconark.network.SRPConnection;

import java.io.IOException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

import roboguice.util.Ln;

/**
 * Created by Anthony on 12/10/2015.
 */
@Singleton
public class ArkService implements OnReceiveListener {

    @Inject
    private SRPConnection connection;

    private OnConnectListener onConnectListener;

    private List<ServerResponseDispatcher> serverResponseDispatchers;

    public ArkService() {
        serverResponseDispatchers = new ArrayList<>();
    }

    public void connect(final Server server) {
        connection.open(server.getHostname(), server.getPort());

        connection.setOnReceiveListener(this);
        connection.setOnConnectListener(new OnConnectListener() {
            @Override
            public void onConnect() {
                login(server.getPassword());
            }

            @Override
            public void onDisconnect() {

            }
        });


    }

    private void login(String password) {
        Packet packet = new Packet(PacketType.SERVERDATA_AUTH.getValue(), password);
        packet.setId(connection.getSequenceNumber());
        try {
            connection.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listPlayers() {
        Packet packet = new Packet(PacketType.SERVERDATA_EXECCOMMAND.getValue(), "ListPlayers");
        try {
            connection.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadcast(String message) {

    }

    public void destroyWildDinos() {

    }

    public void setTimeofDay(int hour, int minute) {

    }

    public void saveWorld() {

    }

    @Override
    public void onReceive(ReceiveEvent event) {
        Packet packet = event.getPacket();

        if (packet.getType() == PacketType.SERVERDATA_RESPONSE_VALUE.getValue()) {
            for (ServerResponseDispatcher dispatcher : serverResponseDispatchers) {

                if(packet.getBody().equals("ListPlayers")){
                    dispatcher.onListPlayers(new ServerResponseEvent(packet));
                }

            }
        }

        if (packet.getType() == PacketType.SERVERDATA_AUTH_RESPONSE.getValue()) {
            if (packet.getId() == -1) {
                try {
                    connection.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Ln.d("Auth Fail");
            } else {
                Ln.d("Auth success");
                onConnectListener.onConnect();
            }
        }
    }

    public void setOnConnectListener(OnConnectListener onConnectListener) {
        this.onConnectListener = onConnectListener;
    }

    public void disconnect() {
        try {
            connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addServerResponseDispatcher(ServerResponseDispatcher dispatcher) {
        this.serverResponseDispatchers.add(dispatcher);
    }

}
