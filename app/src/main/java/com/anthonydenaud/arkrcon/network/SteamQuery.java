package com.anthonydenaud.arkrcon.network;

import com.github.koraktor.steamcondenser.exceptions.SteamCondenserException;
import com.github.koraktor.steamcondenser.servers.SteamPlayer;
import com.github.koraktor.steamcondenser.servers.GoldSrcServer;

import java.nio.BufferUnderflowException;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;

import timber.log.Timber;


public class SteamQuery {

    private GoldSrcServer server;
    private boolean connected;

    public void connect(String hostname, int port) throws SteamQueryException {
        try {
            server = new GoldSrcServer(hostname, port);
            connected = true;
        } catch (SteamCondenserException e) {
            Timber.e(e);
            throw new SteamQueryException();
        }
    }

    public HashMap<String, SteamPlayer> getPlayers() {

        HashMap<String, SteamPlayer> players = new HashMap<>();

        boolean error =  true;
        int retry = 0;
        Throwable cause = null;

        if (server != null) {

            while (error && retry < 3) {
                try {
                    server.updatePlayers();
                    players = server.getPlayers();
                    error = false;
                } catch (SteamCondenserException | TimeoutException | BufferUnderflowException e) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e1) {
                        Timber.e(e1);
                    }
                    cause = e;
                    retry++;
                }
            }
            if(error){
                Timber.w(cause);
            }
        }
        return players;
    }

    /**
     * Retrieve the count of player currently connected.
     * The player count is not retrieved from the server info as this count could be wrong.
     * @return Total
     */
    public int getPlayerCount() {
        int playerCount = 0;
        boolean error =  true;
        int retry = 0;
        Throwable cause = null;
        if(connected)
        {
            while (error && retry < 3){
                try {
                    server.updatePlayers();
                    playerCount = getPlayers().size();
                    error=false;
                } catch (SteamCondenserException | TimeoutException | BufferUnderflowException e) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e1) {
                        Timber.e(e1);
                    }
                    retry++;
                    cause = e;
                }
            }
            if(error){
                Timber.w(cause);
            }
        }
        return playerCount;
    }

    public int getMaxPlayers() {
        int maxPlayers = 0;
        boolean error =  true;
        int retry = 0;
        Throwable cause = null;

        if(connected)
        {
            while (error && retry < 3) {
                try {
                    server.updateServerInfo();
                    HashMap<String, Object> serverInfo = server.getServerInfo();
                    if (serverInfo == null){
                        throw  new SteamCondenserException();
                    }
                    Byte maxPlayersByte = (Byte) serverInfo.get("maxPlayers");
                    if (maxPlayersByte == null){
                        throw  new SteamCondenserException();
                    }
                    maxPlayers = maxPlayersByte & 0xff;
                    error = false;
                } catch (SteamCondenserException | TimeoutException | BufferUnderflowException e) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e1) {
                        Timber.e(e1);
                    }
                    cause = e;
                    retry++;
                }
            }
            if(error){
                Timber.w(cause);
            }
        }
        return maxPlayers;
    }

    public String getServerName() throws SteamQueryException {
        String serverName = "";
        boolean error =  true;
        int retry = 0;
        Throwable cause = null;
        if(connected){
            while (error && retry < 3) {
                try {
                    server.updateServerInfo();
                    HashMap<String, Object> serverInfo = server.getServerInfo();
                    if (serverInfo == null){
                        throw  new SteamCondenserException();
                    }
                    serverName = (String) serverInfo.get("serverName");
                    error = false;
                } catch (SteamCondenserException | TimeoutException | BufferUnderflowException e) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e1) {
                        Timber.e(e1);
                    }
                    cause = e;
                    retry++;
                }
            }
            if(error){
                Timber.w(cause);
                throw new SteamQueryException();
            }
        }
        return serverName;
    }
}
