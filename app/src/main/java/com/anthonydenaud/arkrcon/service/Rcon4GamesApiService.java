package com.anthonydenaud.arkrcon.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.anthonydenaud.arkrcon.api.ApiCallback;
import com.anthonydenaud.arkrcon.api.Rcon4GamesApi;
import com.anthonydenaud.arkrcon.model.Server;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.UUID;

import roboguice.util.Ln;


@Singleton
public class Rcon4GamesApiService {

    private final Context context;
    @Inject
    private Rcon4GamesApi api;

    @Inject
    public Rcon4GamesApiService(Context context) {
        this.context = context;
    }

    public void checkAppUpdateAvailable(ApiCallback apiCallback) {
        api.getLastVersion(apiCallback);
    }


    public void saveUser() {
        String uuid;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.contains("uuid")) {
            uuid = sharedPreferences.getString("uuid", null);
        } else {
            uuid = UUID.randomUUID().toString();
            sharedPreferences.edit().putString("uuid", uuid).apply();
        }
        api.saveUser(uuid);
    }

    public void saveServer(Server server) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        String uuid = server.getUuid();
        String name = server.getName();
        String hostname = server.getHostname();
        int rcon_port = server.getPort();
        int query_port = server.getQueryPort();

        String user_uuid = sharedPreferences.getString("uuid", null);

        if(user_uuid == null){
            Ln.e("User uuid is null.");
        }else{
            api.saveServer(uuid, name, hostname, rcon_port, query_port, user_uuid);
        }
    }
}
