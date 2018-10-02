package com.purplepanda.wspologarniacz.task;

public class TaskNotFoundException extends RuntimeException {
    TaskNotFoundException() {
        super("Could not find task woth given ID");
    }
}
