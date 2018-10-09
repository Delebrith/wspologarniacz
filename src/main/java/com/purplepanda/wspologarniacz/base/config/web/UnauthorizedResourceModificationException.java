package com.purplepanda.wspologarniacz.base.config.web;

public class UnauthorizedResourceModificationException extends RuntimeException {
    public UnauthorizedResourceModificationException() {
        super("User unauthorized to modify requested resource");
    }
}
