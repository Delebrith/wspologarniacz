package com.purplepanda.wspologarniacz.user.exception;

public class IncorrectToken extends RuntimeException {
    public IncorrectToken() {
        super("Incorrect request. Timeout or incorrect data.");
    }
}
