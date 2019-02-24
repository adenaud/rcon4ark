package com.anthonydenaud.arkrcon;

import android.app.Application;

import toothpick.Scope;
import toothpick.Toothpick;

public class RconApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Scope appScope = Toothpick.openScope(this);
        appScope.installModules(new ToothPickModule(this));
    }
}
