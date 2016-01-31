package com.anthonydenaud.arkrcon.fragment;

import com.anthonydenaud.arkrcon.event.ServerResponseDispatcher;
import com.anthonydenaud.arkrcon.model.Player;

import java.util.List;

import roboguice.fragment.RoboFragment;

/**
 * Created by Anthony on 13/10/2015.
 */
public class RconFragment extends RoboFragment implements ServerResponseDispatcher {
    @Override
    public void onListPlayers(List<Player> players) {

    }

    @Override
    public void onGetChat(String chatBuffer) {

    }

    @Override
    public void onGetLog(String logBuffer) {

    }

    @Override
    public void onCustomCommandResult(String result) {

    }

    @Override
    public void onPlayerJoinLeft() {

    }


}
