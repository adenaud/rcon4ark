package com.anthonydenaud.arkrcon.api;

/**
 * Created by Anthony on 08/12/2016.
 */

public class ApiException extends RuntimeException{
    public ApiException(String message) {
        super(message);
    }
}
