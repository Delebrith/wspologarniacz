package com.purplepanda.wspologarniacz.task;

public interface TaskService {
    void deleteTask(Long taskId);

    Task markAsDone(Long taskId);

    Task modify(Task task);
}
