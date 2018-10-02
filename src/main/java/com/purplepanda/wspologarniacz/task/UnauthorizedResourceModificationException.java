package com.purplepanda.wspologarniacz.task;

public class UnauthorizedResourceModificationException extends RuntimeException {
    UnauthorizedResourceModificationException() {
        super("User unauthorized to modify requested resource");
    }
}
