package com.anthonydenaud.rconark.event;

public interface ConnectionListener {
    void onConnect();
    void onDisconnect();
    void onConnectionFail(String message);
}
