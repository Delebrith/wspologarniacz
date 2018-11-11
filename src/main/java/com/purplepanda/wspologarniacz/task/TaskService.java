package com.purplepanda.wspologarniacz.task;

public interface TaskService {
    Task findTask(Long taskId);

    void deleteTask(Task task);

    Task markAsDone(Task task);

    Task modify(Task task, String name, String description);
}
