package net.nexusrcon.nexusrconark.event;

import net.nexusrcon.nexusrconark.model.Player;

import java.util.List;

/**
 * Created by Anthony on 13/10/2015.
 */
public interface ServerResponseDispatcher {
    void onListPlayers(List<Player> players);

}
