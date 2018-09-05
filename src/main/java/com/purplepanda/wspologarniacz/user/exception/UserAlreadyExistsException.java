package com.purplepanda.wspologarniacz.user.exception;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException() {
        super("User with given email already exists in our database.");
    }
}
