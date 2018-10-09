package com.purplepanda.wspologarniacz.base.config.web;

public class InvalidResourceStateException extends RuntimeException {
    public InvalidResourceStateException() {
        super("Requested resource state does not allow modifications");
    }
}
