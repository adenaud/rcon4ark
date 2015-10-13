package net.nexusrcon.nexusrconark.fargment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.inject.Inject;

import net.nexusrcon.nexusrconark.ArkService;
import net.nexusrcon.nexusrconark.R;
import net.nexusrcon.nexusrconark.event.ServerResponseEvent;

import roboguice.inject.InjectView;

/**
 * Created by Anthony on 13/10/2015.
 */
public class PlayersFragment extends RconFragment {

    @Inject
    private ArkService arkService;

    @InjectView(R.id.list_players)
    private ListView listViewPlayers;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_rcon_players, container, false);
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        arkService.listPlayers();
    }

    @Override
    public void onListPlayers(ServerResponseEvent event) {
        String playersStr = event.getPacket().getBody();
    }
}
