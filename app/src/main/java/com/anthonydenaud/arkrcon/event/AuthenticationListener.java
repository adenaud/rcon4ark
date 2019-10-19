package com.anthonydenaud.arkrcon.event;

public interface AuthenticationListener {
    void onAuthenticationSuccess();
    void onAuthenticationFail();
}
