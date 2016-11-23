package com.anthonydenaud.arkrcon.network;

import com.github.koraktor.steamcondenser.exceptions.SteamCondenserException;
import com.github.koraktor.steamcondenser.steam.SteamPlayer;
import com.github.koraktor.steamcondenser.steam.servers.GoldSrcServer;

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

        if (server != null) {

            try {
                server.updatePlayers();
                players = server.getPlayers();
            } catch (SteamCondenserException e) {
                Ln.e(e.getMessage());
            } catch (TimeoutException e) {
                Ln.e("SteamQuery :  TimeoutException");
            }
        }
        return players;
    }

    public int getPlayerCount() {
        int playerCount = 0;
        if(connected)
        {
            try {
                HashMap<String, Object> serverInfo = server.getServerInfo();
                playerCount = ((Byte) serverInfo.get("numberOfPlayers")).intValue();

            } catch (SteamCondenserException | TimeoutException e) {
                e.printStackTrace();
            }
        }
        return playerCount;
    }

    public int getMaxPlayers() {
        int maxPlayers = 0;
        if(connected)
        {
            try {
                HashMap<String, Object> serverInfo = server.getServerInfo();
                maxPlayers = ((Byte) serverInfo.get("maxPlayers"));
            } catch (SteamCondenserException | TimeoutException e) {
                e.printStackTrace();
            }
        }
        return maxPlayers;
    }

    public String getServerName() throws SteamQueryException {
        String serverName = "";
        if(connected){
            try {
                HashMap<String, Object> serverInfo = server.getServerInfo();
                serverName = (String) serverInfo.get("serverName");
            } catch (SteamCondenserException | TimeoutException e) {
                e.printStackTrace();
                throw new SteamQueryException();
            }
        }
        return serverName;
    }
}
