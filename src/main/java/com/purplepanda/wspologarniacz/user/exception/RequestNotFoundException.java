package com.purplepanda.wspologarniacz.user.exception;

public class RequestNotFoundException extends RuntimeException {
    public RequestNotFoundException() {
        super("Token not registered");
    }
}
