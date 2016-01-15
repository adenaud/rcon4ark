package com.anthonydenaud.arkrcon.event;

import com.anthonydenaud.arkrcon.model.Player;

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
