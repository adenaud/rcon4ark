package com.anthonydenaud.arkrcon.service;

import com.anthonydenaud.arkrcon.event.AuthenticationListener;
import com.anthonydenaud.arkrcon.event.ConnectionListener;
import com.anthonydenaud.arkrcon.event.OnReceiveListener;
import com.anthonydenaud.arkrcon.event.ReceiveEvent;
import com.anthonydenaud.arkrcon.model.Server;
import com.anthonydenaud.arkrcon.network.Packet;
import com.anthonydenaud.arkrcon.network.PacketType;
import com.anthonydenaud.arkrcon.network.SRPConnection;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;

@Singleton
public class ConnectionTestService {

    private SRPConnection connection;
    private boolean isTestInProgress = false;

    @Inject
    ConnectionTestService() {
        this.connection = new SRPConnection();
    }

    public void test(Server server,
                     ConnectionListener connectionListener,
                     AuthenticationListener authenticationListener){
        if (!isTestInProgress) {
            isTestInProgress = true;
            connection.open(server.getHostname(), server.getPort());
            connection.setConnectionListener(new ConnectionListener() {
                @Override
                public void onConnect(boolean reconnect) {
                    login(server.getPassword());
                }

                @Override
                public void onDisconnect() {
                    connectionListener.onDisconnect();
                }

                @Override
                public void onConnectionFail() {
                    connectionListener.onConnectionFail();
                }

                @Override
                public void onConnectionDrop() {
                    connectionListener.onConnectionDrop();
                }
            });

            connection.setOnReceiveListener(event -> {
                Packet packet = event.getPacket();
                if (packet.getType() == PacketType.SERVERDATA_AUTH_RESPONSE.getValue()) {
                    if (packet.getId() == -1) {
                        authenticationListener.onAuthenticationFail();
                    } else {
                        authenticationListener.onAuthenticationSuccess();
                    }
                }
            });
        }
    }

    private void login(String password) {
        Packet packet = new Packet(connection.getSequenceNumber(), PacketType.SERVERDATA_AUTH.getValue(), password);
        connection.send(packet);
    }

    public void close(){
        if(isTestInProgress) {
            try {
                connection.setConnectionListener(null);
                if(connection.isConnected()){
                    connection.close();
                }
            } catch (IOException e) {
                Timber.e(e, "ark service disconnect exception");
            } finally {
                isTestInProgress = false;
            }
        }
    }

    public boolean isTestInProgress() {
        return isTestInProgress;
    }
}
