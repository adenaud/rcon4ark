package net.nexusrcon.nexusrconark.fargment;

import net.nexusrcon.nexusrconark.event.ServerResponseDispatcher;
import net.nexusrcon.nexusrconark.event.ServerResponseEvent;
import net.nexusrcon.nexusrconark.model.Player;

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
