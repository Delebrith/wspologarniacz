package com.purplepanda.wspologarniacz.user.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException() {
        super("Could not find user for given credentials.");
    }
}
