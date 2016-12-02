package com.anthonydenaud.arkrcon.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.anthonydenaud.arkrcon.event.OnServerStopRespondingListener;
import com.anthonydenaud.arkrcon.network.SteamQuery;
import com.anthonydenaud.arkrcon.network.SteamQueryException;
import com.github.koraktor.steamcondenser.steam.SteamPlayer;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.anthonydenaud.arkrcon.R;
import com.anthonydenaud.arkrcon.event.ConnectionListener;
import com.anthonydenaud.arkrcon.event.OnReceiveListener;
import com.anthonydenaud.arkrcon.event.ReceiveEvent;
import com.anthonydenaud.arkrcon.event.ServerResponseDispatcher;
import com.anthonydenaud.arkrcon.model.Player;
import com.anthonydenaud.arkrcon.model.Server;
import com.anthonydenaud.arkrcon.network.Packet;
import com.anthonydenaud.arkrcon.network.PacketType;
import com.anthonydenaud.arkrcon.network.SRPConnection;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import roboguice.util.Ln;

@Singleton
public class ArkService implements OnReceiveListener {

    private final Context context;

    @Inject
    private SRPConnection connection;

    @Inject
    private SteamQuery steamQuery;

    private List<ConnectionListener> connectionListeners;

    private final List<ServerResponseDispatcher> serverResponseDispatchers;

    private List<Integer> customCommands;

    private Server server;
    private Timer logTimer;

    private SharedPreferences preferences;

    @Inject
    public ArkService(Context context) {
        this.context = context;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        connectionListeners = new ArrayList<>();
        serverResponseDispatchers = new ArrayList<>();
        customCommands = new ArrayList<>();
    }

    public void connect(final Server server) {

        this.server = server;

        connection.open(server.getHostname(), server.getPort());

        connection.setOnReceiveListener(this);
        connection.setConnectionListener(new ConnectionListener() {
            @Override
            public void onConnect(boolean reconnecting) {
                login(server.getPassword());
            }

            @Override
            public void onConnectionDrop() {
                connection.reconnect();
            }

            @Override
            public void onDisconnect() {
                sendOnDisconnectEvent();
            }

            @Override
            public void onConnectionFail(String message) {
                for (ConnectionListener listener : connectionListeners) {
                    listener.onConnectionFail(message);
                }
            }
        });

        connection.setOnServerStopRespondingListener(new OnServerStopRespondingListener() {
            @Override
            public void onServerStopResponding() {
                if (logTimer != null) {
                    logTimer.cancel();
                }
                connection.open(server.getHostname(), server.getPort());
            }
        });


    }

    private void login(String password) {
        Ln.d("Login ...");
        Packet packet = new Packet(connection.getSequenceNumber(), PacketType.SERVERDATA_AUTH.getValue(), password);
        connection.send(packet);
    }

    public void listPlayers() {
        Packet packet = new Packet(connection.getSequenceNumber(), PacketType.SERVERDATA_EXECCOMMAND.getValue(), "ListPlayers");
        connection.send(packet);
    }

    /**
     * Broadcast a message to all players on the server.
     *
     * @param message
     */
    public void broadcast(String message) {
        Packet packet = new Packet(connection.getSequenceNumber(), PacketType.SERVERDATA_EXECCOMMAND.getValue(), "Broadcast " + message);
        connection.send(packet);
    }

    private String getAdminName() {
        String adminName = context.getString(R.string.default_admin_name);
        if (StringUtils.isNotEmpty(server.getAdminName())) {
            adminName = server.getAdminName();
        }
        return adminName;
    }

    /**
     * Sends a chat message to all currently connected players.
     *
     * @param message
     */
    public void serverChat(String message) {
        Packet packet = new Packet(connection.getSequenceNumber(), PacketType.SERVERDATA_EXECCOMMAND.getValue(), "ServerChat " + getAdminName() + " : " + message);
        connection.send(packet);
    }


    public void serverChatTo(Player player, String message) {
        Packet packet = new Packet(connection.getSequenceNumber(), PacketType.SERVERDATA_EXECCOMMAND.getValue(), "ServerChatTo \"" + player.getSteamId() + "\" " + getAdminName() + " : " + message);
        connection.send(packet);
    }


    public void destroyWildDinos() {
        Packet packet = new Packet(connection.getSequenceNumber(), PacketType.SERVERDATA_EXECCOMMAND.getValue(), "DestroyWildDinos");
        connection.send(packet);
    }

    public void setTimeofDay(int hour, int minute) {
        String command = "SetTimeOfDay " + String.valueOf(hour) + ":" + String.valueOf(minute);
        Packet packet = new Packet(connection.getSequenceNumber(), PacketType.SERVERDATA_EXECCOMMAND.getValue(), command);
        connection.send(packet);
    }

    public void saveWorld() {
        Packet packet = new Packet(connection.getSequenceNumber(), PacketType.SERVERDATA_EXECCOMMAND.getValue(), "SaveWorld");
        connection.send(packet);
    }

    /**
     * Forcibly disconnect the specified player from the server.
     *
     * @param player Player to kick
     */
    public void kickPlayer(Player player) {
        Packet packet = new Packet(connection.getSequenceNumber(), PacketType.SERVERDATA_EXECCOMMAND.getValue(), "KickPlayer  " + player.getSteamId());
        connection.send(packet);
    }

    /**
     * Add the specified player to the server's banned list.
     *
     * @param player
     */
    public void banPlayer(Player player) {
        Packet packet = new Packet(connection.getSequenceNumber(), PacketType.SERVERDATA_EXECCOMMAND.getValue(), "BanPlayer  " + player.getName());
        connection.send(packet);
    }

    /**
     * Remove the specified player from the server's banned list.
     *
     * @param playerName
     */
    public void unBan(String playerName) {
        Packet packet = new Packet(connection.getSequenceNumber(), PacketType.SERVERDATA_EXECCOMMAND.getValue(), "UnbanPlayer " + playerName);
        connection.send(packet);
    }

    /**
     * Adds the player specified by the their Integer encoded Steam ID to the server's whitelist.
     *
     * @param player
     */
    public void allowPlayerToJoinNoCheck(Player player) {
        Packet packet = new Packet(connection.getSequenceNumber(), PacketType.SERVERDATA_EXECCOMMAND.getValue(), "AllowPlayerToJoinNoCheck " + player.getSteamId());
        connection.send(packet);
    }

    /**
     * Removes the specified player from the server's whitelist.
     *
     * @param steamId
     */
    public void disallowPlayerToJoinNoCheck(String steamId) {
        Packet packet = new Packet(connection.getSequenceNumber(), PacketType.SERVERDATA_EXECCOMMAND.getValue(), "DisallowPlayerToJoinNoCheck  " + steamId);
        connection.send(packet);
    }


    @Override
    public void onReceive(ReceiveEvent event) {
        Packet packet = event.getPacket();

        if (packet.getType() == PacketType.SERVERDATA_RESPONSE_VALUE.getValue()) {

            Packet requestPacket = connection.getRequestPacket(packet.getId());

            List<Player> players = new ArrayList<>();
            if (StringUtils.isNotEmpty(requestPacket.getBody()) && requestPacket.getBody().equals("ListPlayers")){
                players = getPlayers(packet.getBody());
            }

            synchronized (serverResponseDispatchers) {
                for (ServerResponseDispatcher dispatcher : serverResponseDispatchers) {

                    if (requestPacket == null) {
                        Ln.e(String.valueOf(packet.getId()) + packet.getBody());
                    } else if (customCommands.contains(packet.getId())) {
                        dispatcher.onCustomCommandResult(packet.getBody());
                    } else if (StringUtils.isNotEmpty(requestPacket.getBody()) && requestPacket.getBody().equals("ListPlayers")) {
                        dispatcher.onListPlayers(players);
                    } else if (StringUtils.isNotEmpty(requestPacket.getBody()) && requestPacket.getBody().equals("getchat") && !packet.getBody().contains("Server received, But no response!!")) {
                        dispatcher.onGetChat(packet.getBody());
                    } else if (StringUtils.isNotEmpty(requestPacket.getBody()) && requestPacket.getBody().equals("getgamelog") && !packet.getBody().contains("Server received, But no response!!")) {
                        dispatcher.onGetLog(packet.getBody());
                        requestPLayerListUpdate(dispatcher, packet.getBody());
                    }
                }
            }
        }

        if (packet.getType() == PacketType.SERVERDATA_AUTH_RESPONSE.getValue()) {
            if (packet.getId() == -1) {

                for (ConnectionListener listener : connectionListeners) {
                    listener.onConnectionFail(context.getString(R.string.authentication_fail));
                }
                disconnect();
            } else {
                int queryPort = server.getQueryPort();
                try{
                    this.steamQuery.connect(server.getHostname(), queryPort);
                }catch (SteamQueryException e){
                    Ln.e("Unable to connect via Steam condenser",e );
                }


                for (ConnectionListener listener : connectionListeners) {
                    listener.onConnect(connection.isReconnecting());
                }

                startLogAndChatTimers();
            }
        }
    }

    /**
     * Update the player list if a Join or Left message aged of 10min or less is read in the log.
     * @param dispatcher
     * @param logBuffer
     */
    public void requestPLayerListUpdate(ServerResponseDispatcher dispatcher, String logBuffer) {
        if (logBuffer.trim().split("\\n").length < 20) {
            Pattern pattern = Pattern.compile("([0-9]{4})\\.([0-9]{2})\\.([0-9]{2})_([0-9]{2}).([0-9]{2}).([0-9]{2}):(.*)(joined|left) this ARK!!?\\r?\\n");
            Matcher matcher = pattern.matcher(logBuffer);
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            Date date = new Date();
            int count = 0;
            while (matcher.find()) {
                calendar.set(Calendar.YEAR, Integer.valueOf(matcher.group(1)));
                calendar.set(Calendar.MONTH, Integer.valueOf(matcher.group(2)) - 1);
                calendar.set(Calendar.DAY_OF_MONTH, Integer.valueOf(matcher.group(3)));
                calendar.set(Calendar.HOUR_OF_DAY, Integer.valueOf(matcher.group(4)));
                calendar.set(Calendar.MINUTE, Integer.valueOf(matcher.group(5)));
                calendar.set(Calendar.SECOND, Integer.valueOf(matcher.group(6)));
                date = calendar.getTime();
                count++;
            }

            long dateDiff = Math.abs(new Date().getTime() / 1000 - (date.getTime() / 1000));
            if (count > 0 && dateDiff < 600) {
                dispatcher.onPlayerJoinLeft();
            }
        }
    }

    private void startLogAndChatTimers() {

        String command = "getgamelog";

        if (preferences.getBoolean("chat_instead_of_log", false)) {
            command = "getchat";
        }

        int defaultLogDelay = context.getResources().getInteger(R.integer.log_timer_delay);
        int logDelay = Integer.valueOf(preferences.getString("log_delay", String.valueOf(defaultLogDelay)));

        logTimer = new Timer("LogTimer");
        final String finalCommand = command;
        logTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (connection.isConnected()) {
                    Packet packet = new Packet(connection.getSequenceNumber(), PacketType.SERVERDATA_EXECCOMMAND.getValue(), finalCommand);
                    connection.send(packet);
                }
            }
        }, logDelay, logDelay);
    }

    public void disconnect() {
        if (logTimer != null) {
            logTimer.cancel();
        }
        connection.setReconnecting(false);
        try {
            connection.close();
        } catch (IOException e) {
            Ln.e("ark service disconnect exception", e);
        }
    }

    public void addServerResponseDispatcher(ServerResponseDispatcher dispatcher) {
        synchronized (serverResponseDispatchers) {
            if (!serverResponseDispatchers.contains(dispatcher)) {
                this.serverResponseDispatchers.add(dispatcher);
            }
        }
    }

    private List<Player> getPlayers(String messageBody) {
        HashMap<String, SteamPlayer> steamPlayers = steamQuery.getPlayers();
        List<Player> players = new ArrayList<>();
        String[] playersArray = messageBody.split("\n");

        if (!messageBody.startsWith("No Players Connected")) {

            for (String aPlayersArray : playersArray) {
                if (aPlayersArray.length() > 20) { // 20 = playerId + steamId min length

                    Pattern pattern = Pattern.compile("(\\d*)\\. (.+), ([0-9]+) ?");
                    Matcher matcher = pattern.matcher(aPlayersArray);

                    if (matcher.matches()) {

                        String name = matcher.group(2);
                        String steamId = matcher.group(3);
                        Player player = new Player(name, steamId, 0);

                        SteamPlayer steamPlayer = steamPlayers.get(name);
                        if (steamPlayer != null) {
                            player.setConnectTime(steamPlayer.getConnectTime());
                        }

                        players.add(player);
                    }
                }
            }
        }
        return players;
    }

    private void sendOnDisconnectEvent() {
        for (ConnectionListener listener : connectionListeners) {
            listener.onDisconnect();
        }
        removeAllConnectionListener();
    }

    public synchronized void addConnectionListener(ConnectionListener connectionListener) {
        connectionListeners.add(connectionListener);
    }

    public synchronized void removeAllConnectionListener() {
        connectionListeners.clear();
    }

    public void sendRawCommand(String command) {
        int id = connection.getSequenceNumber();
        customCommands.add(id);
        Packet packet = new Packet(id, PacketType.SERVERDATA_EXECCOMMAND.getValue(), command);
            connection.send(packet);
    }

    public void removeServerResponseDispatcher(ServerResponseDispatcher serverResponseDispatcher) {
        synchronized (serverResponseDispatchers) {
            serverResponseDispatchers.remove(serverResponseDispatcher);
        }
    }
}


