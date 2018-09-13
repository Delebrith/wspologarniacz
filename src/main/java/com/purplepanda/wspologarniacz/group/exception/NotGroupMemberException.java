package com.purplepanda.wspologarniacz.group.exception;

public class NotGroupMemberException extends RuntimeException {
    public NotGroupMemberException() {
        super("User is not a member of modified group. Access denied.");
    }
}
