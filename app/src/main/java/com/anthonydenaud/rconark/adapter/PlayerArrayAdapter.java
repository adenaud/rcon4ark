package com.anthonydenaud.rconark.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.inject.Inject;

import com.anthonydenaud.rconark.R;
import com.anthonydenaud.rconark.model.Player;

import java.util.List;


public class PlayerArrayAdapter extends ArrayAdapter<Player> {


    private static final int RESOURCE = R.layout.item_player;
    private final Context context;
    private List<Player> players;

    @Inject
    public PlayerArrayAdapter(Context context) {
        super(context, RESOURCE);
        this.context = context;
    }

    @Override
    public Player getItem(int position) {
        return players.get(position);
    }

    @Override
    public int getCount() {
        return players.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(RESOURCE,null,false);

        Player player = getItem(position);

        TextView textViewName = (TextView) view.findViewById(R.id.textView_player_name);
        textViewName.setText(player.getName());

        return view;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }



}
