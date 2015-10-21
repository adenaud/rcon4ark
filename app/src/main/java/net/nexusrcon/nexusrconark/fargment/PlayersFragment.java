package net.nexusrcon.nexusrconark.fargment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.inject.Inject;

import net.nexusrcon.nexusrconark.adapter.PlayerArrayAdapter;
import net.nexusrcon.nexusrconark.model.Player;
import net.nexusrcon.nexusrconark.model.Server;
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

    @Inject
    private PlayerArrayAdapter playerArrayAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        registerForContextMenu(listViewPlayers);
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
    public void onListPlayers(List<Player> players) {

        if(players.size() > 0){
            playerArrayAdapter.setPlayers(players);
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textViewNoPlayers.setVisibility(View.GONE);
                    listViewPlayers.setAdapter(playerArrayAdapter);
                }
            });
        }
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.menu_players_floating, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Player player = (Player) listViewPlayers.getItemAtPosition(info.position);
        switch (item.getItemId()) {

            case R.id.menu_action_kill:
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }
}
