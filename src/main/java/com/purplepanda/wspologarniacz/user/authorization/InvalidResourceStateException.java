package com.purplepanda.wspologarniacz.user.authorization;

public class InvalidResourceStateException extends RuntimeException {
    public InvalidResourceStateException() {
        super("Requested resource state does not allow modifications");
    }
}
