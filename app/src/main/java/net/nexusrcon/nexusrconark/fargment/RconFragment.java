package net.nexusrcon.nexusrconark.fargment;

import net.nexusrcon.nexusrconark.event.ServerResponseDispatcher;
import net.nexusrcon.nexusrconark.event.ServerResponseEvent;

import roboguice.fragment.RoboFragment;

/**
 * Created by Anthony on 13/10/2015.
 */
public class RconFragment extends RoboFragment implements ServerResponseDispatcher {
    @Override
    public void onListPlayers(ServerResponseEvent event) {

    }
}
