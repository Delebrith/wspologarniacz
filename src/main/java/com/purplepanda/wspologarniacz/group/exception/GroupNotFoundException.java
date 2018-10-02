package com.purplepanda.wspologarniacz.group.exception;

public class GroupNotFoundException extends RuntimeException {
    public GroupNotFoundException() {
        super("Group does not exist");
    }
}
