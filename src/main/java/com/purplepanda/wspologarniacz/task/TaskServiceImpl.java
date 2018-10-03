package com.purplepanda.wspologarniacz.task;

import com.purplepanda.wspologarniacz.user.User;
import com.purplepanda.wspologarniacz.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public void deleteTask(Long taskId) {
        Task task = taskRepository.findById(taskId).orElseThrow(TaskNotFoundException::new);
        authorize(task);
        taskRepository.delete(task);
    }

    @Override
    public Task markAsDone(Long taskId) {
        Task marked = taskRepository.findById(taskId).orElseThrow(TaskNotFoundException::new);
        authorize(marked);
        marked.setStatus(TaskStatus.DONE);
        marked.setLastModifiedBy(userService.getAuthenticatedUser());
        marked.setUpdateTime(LocalDateTime.now());
        return taskRepository.save(marked);
    }

    @Override
    public Task modify(Task task) {
        Task modified = taskRepository.findById(task.getId()).orElseThrow(TaskNotFoundException::new);
        authorize(modified);
        if (!modified.getStatus().equals(task.getStatus()))
            throw new UnauthorizedResourceModificationException();
        modified.setLastModifiedBy(userService.getAuthenticatedUser());
        modified.setUpdateTime(LocalDateTime.now());
        return taskRepository.save(task);
    }

    private void authorize(Task task) {
        User authenticated = userService.getAuthenticatedUser();
        task.getAuthorized().stream()
                .filter(u -> authenticated.equals(u))
                .findFirst()
                .orElseThrow(UnauthorizedResourceModificationException::new);
    }
}
