package com.anthonydenaud.arkrcon.service;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.anthonydenaud.arkrcon.api.ApiCallback;
import com.anthonydenaud.arkrcon.api.Rcon4GamesApi;
import com.anthonydenaud.arkrcon.model.Server;

import java.util.UUID;


import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;


@Singleton
public class Rcon4GamesApiService {

    private final Rcon4GamesApi api;

    @Inject
    public Rcon4GamesApiService() {
        this.api = new Rcon4GamesApi();
    }

    public void checkAppUpdateAvailable(ApiCallback apiCallback) {
        api.getLastVersion(apiCallback);
    }
}
