package net.nexusrcon.nexusrconark.event;

public interface ConnectionListener {
    void onConnect();
    void onDisconnect();
    void onConnectionFail(String message);
}
