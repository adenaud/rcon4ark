package com.anthonydenaud.arkrcon.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.inject.Inject;

import com.anthonydenaud.arkrcon.R;
import com.anthonydenaud.arkrcon.dao.ServerDAO;
import com.anthonydenaud.arkrcon.model.Server;

import java.util.List;

/**
 * Created by Anthony on 09/10/2015.
 */
public class ServerAdapter extends ArrayAdapter<Server> {

    private static final int RESOURCE = R.layout.item_server;
    private List<Server> servers;
    private Context context;
    private ServerDAO serverDAO;

    @Inject
    public ServerAdapter(Context context, ServerDAO serverDAO) {
        super(context, RESOURCE );
        this.context = context;
        this.serverDAO = serverDAO;

        servers = serverDAO.findAll();
    }

    @Override
    public Server getItem(int position) {
        return servers.get(position);
    }

    @Override
    public int getCount() {
        return servers.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(RESOURCE,null,false);

        Server server = getItem(position);

        TextView textViewName = (TextView) view.findViewById(R.id.textView_server_name);
        textViewName.setText(server.getName());

        TextView textViewDetails = (TextView) view.findViewById(R.id.textView_server_details);
        textViewDetails.setText(server.getHostname() + ":" + String.valueOf(server.getPort()));

        return view;
    }

    public void refresh() {
        servers = serverDAO.findAll();
    }
}
