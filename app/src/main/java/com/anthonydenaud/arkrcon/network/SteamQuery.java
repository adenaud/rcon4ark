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

        if (server != null) {

            try {
                server.updatePlayers();
                players = server.getPlayers();
            } catch (SteamCondenserException | TimeoutException | BufferUnderflowException e) {
                Ln.e(e.getMessage());
                RavenLogger.getInstance().error(SteamQuery.class, "getPlayers error", e);
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
        if(connected)
        {
            try {
                server.updatePlayers();
                playerCount = getPlayers().size();

            } catch (SteamCondenserException | TimeoutException | BufferUnderflowException e) {
                Ln.w(e.getMessage());
                RavenLogger.getInstance().warn(SteamQuery.class, "getPlayerCount error", e);
            }
        }
        return playerCount;
    }

    public int getMaxPlayers() {
        int maxPlayers = 0;
        if(connected)
        {
            try {
                server.updateServerInfo();
                HashMap<String, Object> serverInfo = server.getServerInfo();
                maxPlayers = ((Byte) serverInfo.get("maxPlayers"));
            } catch (SteamCondenserException | TimeoutException | BufferUnderflowException e) {
                Ln.w(e.getMessage());
                RavenLogger.getInstance().warn(SteamQuery.class, "getMaxPlayers error", e);
            }
        }
        return maxPlayers;
    }

    public String getServerName() throws SteamQueryException {
        String serverName = "";
        if(connected){
            try {
                server.updateServerInfo();
                HashMap<String, Object> serverInfo = server.getServerInfo();
                serverName = (String) serverInfo.get("serverName");
            } catch (SteamCondenserException | TimeoutException | BufferUnderflowException e) {
                Ln.w(e.getMessage());
                RavenLogger.getInstance().warn(SteamQuery.class, "getServerName error", e);
                throw new SteamQueryException();
            }
        }
        return serverName;
    }
}
