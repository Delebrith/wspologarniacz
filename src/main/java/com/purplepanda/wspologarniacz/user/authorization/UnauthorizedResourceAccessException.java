package com.purplepanda.wspologarniacz.user.authorization;

public class UnauthorizedResourceAccessException extends RuntimeException {
    public UnauthorizedResourceAccessException() {
        super("User unauthorized to modify requested resource");
    }
}
