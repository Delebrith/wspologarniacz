package com.purplepanda.wspologarniacz.group.exception;

public class InvalidAffiliationStateException extends RuntimeException {
    public InvalidAffiliationStateException() {
        super("Invalid state of user's affiliation to group. Cannot perform requested operation");
    }
}
