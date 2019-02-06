package com.purplepanda.wspologarniacz.user.authorization;

public class InvalidModificationAttemptException extends RuntimeException {
    public InvalidModificationAttemptException(){
        super("Invalid modification data");
    }
}
