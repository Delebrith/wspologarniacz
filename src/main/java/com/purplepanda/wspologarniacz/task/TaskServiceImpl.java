package com.purplepanda.wspologarniacz.task;

import com.purplepanda.wspologarniacz.base.config.web.InvalidResourceStateException;
import com.purplepanda.wspologarniacz.base.config.web.UnauthorizedResourceModificationException;
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
//        authorize(task);
        taskRepository.delete(task);
    }

    @Override
    public Task markAsDone(Long taskId) {
        Task marked = taskRepository.findById(taskId).orElseThrow(TaskNotFoundException::new);
//        authorize(marked);
        if (marked.getStatus().equals(TaskStatus.DONE))
            throw new InvalidResourceStateException();
        marked.setStatus(TaskStatus.DONE);
        marked.setLastModifiedBy(userService.getAuthenticatedUser());
        marked.setUpdateTime(LocalDateTime.now());
        return taskRepository.save(marked);
    }

    @Override
    public Task modify(Long taskId, String name, String description) {
        Task modified = taskRepository.findById(taskId).orElseThrow(TaskNotFoundException::new);
//        authorize(modified);

        if (modified.getStatus().equals(TaskStatus.DONE))
            throw new InvalidResourceStateException();

        modified.setName(name);
        modified.setDescription(description);
        modified.setLastModifiedBy(userService.getAuthenticatedUser());
        modified.setUpdateTime(LocalDateTime.now());
        return taskRepository.save(modified);
    }

//    private void authorize(Task task) {
//        User authenticated = userService.getAuthenticatedUser();
//        task.getAuthorized().stream()
//                .filter(u -> authenticated.equals(u))
//                .findFirst()
//                .orElseThrow(UnauthorizedResourceModificationException::new);
//    }
}
