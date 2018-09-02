package com.purplepanda.wspologarniacz.user.exception;

public class IncorrectRequestState extends RuntimeException {
    public IncorrectRequestState() {
        super("Incorrect request. Timeout or incorrect data.");
    }
}
