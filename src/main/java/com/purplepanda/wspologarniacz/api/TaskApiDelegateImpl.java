package com.purplepanda.wspologarniacz.api;

import com.purplepanda.wspologarniacz.api.model.TaskDto;
import com.purplepanda.wspologarniacz.api.model.TaskInfoDto;
import com.purplepanda.wspologarniacz.task.Task;
import com.purplepanda.wspologarniacz.task.TaskMapper;
import com.purplepanda.wspologarniacz.task.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class TaskApiDelegateImpl implements TaskApiDelegate {

    private final TaskService taskService;
    private final TaskMapper taskMapper = TaskMapper.getInstance();

    @Autowired
    public TaskApiDelegateImpl(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public ResponseEntity<Void> deleteTask(Long taskId) {
        Task task = taskService.findTask(taskId);
        taskService.deleteTask(task);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<TaskDto> markTaskAsDone(Long taskId) {
        Task task = taskService.findTask(taskId);
        return ResponseEntity.ok(taskMapper.toDto(taskService.markAsDone(task)));
    }

    @Override
    public ResponseEntity<TaskDto> modify(Long taskId, TaskInfoDto taskInfoDto) {
        Task task = taskService.findTask(taskId);
        return ResponseEntity.ok(taskMapper.toDto(
                taskService.modify(task, taskInfoDto.getName(), taskInfoDto.getDescription())
        ));
    }
}
