package com.anthonydenaud.arkrcon.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.inject.Inject;

import com.anthonydenaud.arkrcon.R;
import com.anthonydenaud.arkrcon.model.Player;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


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
        convertView = inflater.inflate(RESOURCE, null, false);

        Player player = getItem(position);

        TextView textViewName = (TextView) convertView.findViewById(R.id.textView_player_name);
        TextView textViewTime = (TextView) convertView.findViewById(R.id.textView_player_connect_time);
        textViewName.setText(player.getName());
        textViewTime.setText(formatTime(player.getConnectTime()));

        return convertView;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }


    private String formatTime(float time) {
        long longVal = (long) time;
        int hours = (int) longVal / 3600;
        int remainder = (int) longVal - hours * 3600;
        int minutes = remainder / 60;
        remainder = remainder - minutes * 60;
        int seconds = remainder;

        if (time == 0) {
            return " - ";
        } else {
            if (hours == 0 && minutes == 0) {
                return String.format(Locale.ENGLISH, "%02ds", seconds);
            } else if (hours == 0) {
                return String.format(Locale.ENGLISH, "%02dm %02ds", minutes, seconds);
            } else {
                return String.format(Locale.ENGLISH, "%dh %02dm %02ds", hours, minutes, seconds);
            }
        }
    }

}
