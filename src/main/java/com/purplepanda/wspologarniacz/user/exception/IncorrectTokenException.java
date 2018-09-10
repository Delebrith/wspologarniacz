package com.purplepanda.wspologarniacz.user.exception;

public class IncorrectTokenException extends RuntimeException {
    public IncorrectTokenException() {
        super("Incorrect request. Timeout or incorrect data.");
    }
}
