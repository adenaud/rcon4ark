package com.anthonydenaud.rconark.fargment;

import com.anthonydenaud.rconark.event.ServerResponseDispatcher;
import com.anthonydenaud.rconark.model.Player;

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
}
