package net.nexusrcon.nexusrconark.fargment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.inject.Inject;

import net.nexusrcon.nexusrconark.service.ArkService;
import net.nexusrcon.nexusrconark.R;
import net.nexusrcon.nexusrconark.event.ServerResponseEvent;

import java.util.ArrayList;
import java.util.List;

import roboguice.inject.InjectView;

public class PlayersFragment extends RconFragment {

    @Inject
    private ArkService arkService;

    @InjectView(R.id.list_players)
    private ListView listViewPlayers;

    @InjectView(R.id.textview_noplayers)
    private TextView textViewNoPlayers;

    private Activity context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_rcon_players, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        arkService.addServerResponseDispatcher(this);
        arkService.listPlayers();
    }

    @Override
    public void onAttach(Context context) {
        this.context = (Activity) context;
        super.onAttach(context);
    }

    @Override
    public void onListPlayers(ServerResponseEvent event) {
        String playersStr = event.getPacket().getBody();

        if(!playersStr.startsWith("No Players Connected")){

            List<String> players = new ArrayList<>();
            String[] playersArray = playersStr.split("\n");

            for (int i = 0; i<playersArray.length; i++) {
                if (playersArray[i].length() > 20) { // 20 = playerId + steamId min length
                    players.add(playersArray[i]);
                }
            }

            final ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, players);
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textViewNoPlayers.setVisibility(View.GONE);
                    listViewPlayers.setAdapter(adapter);
                }
            });
        }
    }
}
