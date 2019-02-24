package com.anthonydenaud.arkrcon.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.anthonydenaud.arkrcon.adapter.PlayerArrayAdapter;
import com.anthonydenaud.arkrcon.model.Player;
import com.anthonydenaud.arkrcon.service.ArkService;
import com.anthonydenaud.arkrcon.R;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import toothpick.Scope;
import toothpick.Toothpick;

public class PlayersFragment extends RconFragment {

    @BindView(R.id.list_players)
    ListView listViewPlayers;

    @BindView(R.id.textview_noplayers)
    TextView textViewNoPlayers;

    @Inject
    ArkService arkService;
    private Activity context;

    private PlayerArrayAdapter playerArrayAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = getActivity();
        this.playerArrayAdapter = new PlayerArrayAdapter(context);

        Scope s = Toothpick.openScopes(getActivity().getApplication(), this);
        Toothpick.inject(this, s);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view =  inflater.inflate(R.layout.fragment_rcon_players, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        registerForContextMenu(listViewPlayers);

        arkService.addServerResponseDispatcher(this);
        arkService.listPlayers();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        getActivity().getMenuInflater().inflate(R.menu.menu_players, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menu_action_refresh){
            arkService.listPlayers();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Context context) {
        this.context = (Activity) context;
        super.onAttach(context);
    }

    @Override
    public void onDestroy() {
        arkService.removeServerResponseDispatcher(this);
        super.onDestroy();
    }

    @Override
    public void onListPlayers(List<Player> players) {

        if (players.size() > 0) {
            playerArrayAdapter.setPlayers(players);
            context.runOnUiThread(() -> {
                textViewNoPlayers.setVisibility(View.GONE);
                listViewPlayers.setAdapter(playerArrayAdapter);
            });
        }
    }

    @Override
    public void onPlayerJoinLeft() {
        arkService.listPlayers();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.menu_players_floating, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip;

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Player player = (Player) listViewPlayers.getItemAtPosition(info.position);
        switch (item.getItemId()) {
            case R.id.menu_send_message:
                openMessageDialog(player);
                return true;
            case R.id.menu_action_kick:
                arkService.kickPlayer(player);
                return true;
            case R.id.menu_action_ban:
                arkService.banPlayer(player);
                return true;
            case R.id.menu_action_whitelist:
                arkService.allowPlayerToJoinNoCheck(player);
                return true;

            case R.id.menu_cpy_name:
                clip = ClipData.newPlainText("player_name", player.getName());
                clipboard.setPrimaryClip(clip);
                return true;
            case R.id.menu_cpy_steamid:
                clip = ClipData.newPlainText("player_steamid", player.getSteamId());
                clipboard.setPrimaryClip(clip);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void openMessageDialog(final Player player) {
        final EditText editText = new EditText(context);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.send_message);
        builder.setView(editText);
        builder.setPositiveButton(R.string.ok, (dialog, which) -> arkService.serverChatTo(player, editText.getText().toString()));
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }
}
