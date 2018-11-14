package com.purplepanda.wspologarniacz.task;

import com.purplepanda.wspologarniacz.user.authorization.InvalidResourceStateException;
import com.purplepanda.wspologarniacz.group.GroupMemberListUpdatedEvent;
import com.purplepanda.wspologarniacz.user.UserService;
import com.purplepanda.wspologarniacz.user.authorization.ResourceAccessAuthorization;
import com.purplepanda.wspologarniacz.user.authorization.ResourceModificationAuthorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TaskServiceImpl implements TaskService {

    private final UserService userService;
    private final TaskRepository taskRepository;

    @Autowired
    public TaskServiceImpl(UserService userService, TaskRepository taskRepository) {
        this.userService = userService;
        this.taskRepository = taskRepository;
    }

    @Override
    public Task findTask(Long taskId) {
        return taskRepository.findById(taskId).orElseThrow(TaskNotFoundException::new);
    }

    @Override
    @ResourceModificationAuthorization
    public void deleteTask(Task task) {
        taskRepository.delete(task);
    }

    @Override
    @ResourceModificationAuthorization
    public Task markAsDone(Task task) {
        if (task.getStatus().equals(TaskStatus.DONE))
            throw new InvalidResourceStateException();
        task.setStatus(TaskStatus.DONE);
        task.setLastModifiedBy(userService.getAuthenticatedUser());
        task.setUpdateTime(LocalDateTime.now());
        return taskRepository.save(task);
    }

    @Override
    @ResourceModificationAuthorization
    public Task modify(Task task, String name, String description) {
        if (task.getStatus().equals(TaskStatus.DONE))
            throw new InvalidResourceStateException();

        task.setName(name);
        task.setDescription(description);
        task.setLastModifiedBy(userService.getAuthenticatedUser());
        task.setUpdateTime(LocalDateTime.now());
        return taskRepository.save(task);
    }

    @EventListener
    public void handleEvent(GroupMemberListUpdatedEvent event) {
        event.getGroup().getTasks().stream()
                .forEach(t -> {
                    t.setAuthorized(event.getMembers());
                    taskRepository.save(t);
                });
    }

}
