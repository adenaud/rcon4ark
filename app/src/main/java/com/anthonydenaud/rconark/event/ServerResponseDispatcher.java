package com.anthonydenaud.rconark.event;

import com.anthonydenaud.rconark.model.Player;

import java.util.List;

/**
 * Created by Anthony on 13/10/2015.
 */
public interface ServerResponseDispatcher {
    void onListPlayers(List<Player> players);
    void onGetChat(String chatBuffer);
    void onGetLog(String logBuffer);
    void onCustomCommandResult(String result);
}
