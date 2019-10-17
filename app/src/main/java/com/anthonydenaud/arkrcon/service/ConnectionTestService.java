package com.anthonydenaud.arkrcon.service;

import com.anthonydenaud.arkrcon.event.ConnectionListener;
import com.anthonydenaud.arkrcon.model.Server;
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

    public void test(Server server, ConnectionListener connectionListener){
        if (!isTestInProgress) {
            isTestInProgress = true;
            connection.open(server.getHostname(), server.getPort(), false);
            connection.setConnectionListener(connectionListener);
        }
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
