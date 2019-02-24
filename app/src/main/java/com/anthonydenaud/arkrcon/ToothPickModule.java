package com.anthonydenaud.arkrcon;

import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;

import com.anthonydenaud.arkrcon.dao.DatabaseHelper;

import toothpick.config.Module;
import toothpick.smoothie.provider.SharedPreferencesProvider;
import toothpick.smoothie.provider.SystemServiceProvider;

class ToothPickModule extends Module {

    ToothPickModule(Application application){
        bind(Application.class).toInstance(application);
        bind(SharedPreferences.class).toProviderInstance(new SharedPreferencesProvider(application, "preferences"));
        bind(NotificationManager.class).toProviderInstance(new SystemServiceProvider<>(application, Context.NOTIFICATION_SERVICE));
        bind(DatabaseHelper.class).toInstance(new DatabaseHelper(application));
    }
}
