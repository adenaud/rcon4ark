package com.anthonydenaud.rconark.event;

public interface ConnectionListener {
    void onConnect(boolean reconnect);
    void onDisconnect();
    void onConnectionFail(String message);
}
