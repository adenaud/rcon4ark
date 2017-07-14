package com.anthonydenaud.arkrcon.network;

import com.anthonydenaud.arkrcon.RavenLogger;
import com.github.koraktor.steamcondenser.exceptions.SteamCondenserException;
import com.github.koraktor.steamcondenser.steam.SteamPlayer;
import com.github.koraktor.steamcondenser.steam.servers.GoldSrcServer;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import roboguice.util.Ln;

public class SteamQuery {

    private GoldSrcServer server;
    private boolean connected;

    public void connect(String hostname, int port) throws SteamQueryException {
        try {
            server = new GoldSrcServer(hostname, port);
            connected = true;
        } catch (SteamCondenserException e) {
            Ln.e(e.getMessage());
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
                        Ln.e(e1.getMessage(), e1);
                        RavenLogger.getInstance().error(SteamQuery.class, "getPlayers InterruptedException", e1);
                    }
                    cause = e;
                    retry++;
                }
            }
            if(error){
                Ln.w(cause.getMessage());
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
                        Ln.e(e1.getMessage(), e1);
                        RavenLogger.getInstance().error(SteamQuery.class, "getPlayerCount InterruptedException", e1);
                    }
                    retry++;
                    cause = e;
                }
            }
            if(error){
                Ln.w(cause.getMessage());
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
                    if(serverInfo == null){
                        throw  new SteamCondenserException();
                    }
                    maxPlayers = ((Byte) serverInfo.get("maxPlayers"));
                    error = false;
                } catch (SteamCondenserException | TimeoutException | BufferUnderflowException e) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e1) {
                        Ln.e(e1.getMessage(), e1);
                        RavenLogger.getInstance().error(SteamQuery.class, "getMaxPlayers InterruptedException", e1);
                    }
                    cause = e;
                    retry++;
                }
            }
            if(error){
                Ln.w(cause.getMessage());
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
                    serverName = (String) serverInfo.get("serverName");
                    error = false;
                } catch (SteamCondenserException | TimeoutException | BufferUnderflowException e) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e1) {
                        Ln.e(e1.getMessage(), e1);
                        RavenLogger.getInstance().error(SteamQuery.class, "getServerName InterruptedException", e1);
                    }
                    cause = e;
                    retry++;
                }
            }
            if(error){
                Ln.w(cause.getMessage());
                throw new SteamQueryException();
            }
        }
        return serverName;
    }
}
