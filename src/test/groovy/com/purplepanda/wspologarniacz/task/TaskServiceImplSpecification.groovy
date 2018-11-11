package com.purplepanda.wspologarniacz.task

import com.purplepanda.wspologarniacz.api.TaskApiDelegate
import com.purplepanda.wspologarniacz.api.TaskApiDelegateImpl
import com.purplepanda.wspologarniacz.api.model.TaskInfoDto
import com.purplepanda.wspologarniacz.base.config.web.InvalidResourceStateException
import com.purplepanda.wspologarniacz.base.config.web.UnauthorizedResourceModificationException
import com.purplepanda.wspologarniacz.user.AuthorityName
import com.purplepanda.wspologarniacz.user.User
import com.purplepanda.wspologarniacz.user.UserService
import org.springframework.http.ResponseEntity
import spock.lang.Specification

import java.time.LocalDateTime

class TaskServiceImplSpecification extends Specification {


    //tested
    private TaskService taskService

    //mocked
    private TaskRepository taskRepository
    private UserService userService

    //test data
    private User authenticated
    private Task task

    void setup() {
        userService = Mock(UserService.class)
        taskRepository = Mock(TaskRepository.class)
        taskService = new TaskServiceImpl(userService, taskRepository)

        authenticated = User.builder()
                .id(1L)
                .name("user")
                .active(true)
                .authorities(Collections.singletonList(AuthorityName.USER))
                .build()

        task = Task.builder()
                .name("task")
                .status(TaskStatus.ADDED)
                .updateTime(LocalDateTime.now())
                .build()
        task.id = 1L
    }

    //mark as done
    void "authorized user should successfully mark task as done"() {
        given: "task for authenticated user"
        userService.getAuthenticatedUser() >> authenticated
        task.authorized.add(authenticated)
        taskRepository.findById(task.id) >> Optional.ofNullable(task)
        taskRepository.save(task) >> task

        when: "user marks task as done"
        Task result = taskService.markAsDone(task)

        then: "task is marked"
        result.id == task.id
        result.status == TaskStatus.DONE
    }

    void "user should fail to mark done task as done"() {
        given: "non-existing task"
        userService.getAuthenticatedUser() >> authenticated
        task.authorized.add(authenticated)
        task.status = TaskStatus.DONE
        taskRepository.findById(task.id) >> Optional.ofNullable(task)

        when: "user marks task as done"
        taskService.markAsDone(task)

        then: "exception is thrown"
        thrown(InvalidResourceStateException.class)
    }

    //modify
    void "authorized user should successfully modify task"() {
        given: "task for authenticated user"
        userService.getAuthenticatedUser() >> authenticated
        String newName = "new name"
        String newDescription = "new description"
        task.authorized.add(authenticated)
        taskRepository.findById(task.id) >> Optional.ofNullable(task)
        taskRepository.save(task) >> task

        when: "user modifies task"
        Task result = taskService.modify(task, newName, newDescription)

        then: "task is modified"
        result.id == task.id
        result.name == newName
        result.description == newDescription
    }

    void "user should fail to modify done task"() {
        given: "non-existing task"
        userService.getAuthenticatedUser() >> authenticated
        String newName = "new name"
        String newDescription = "new description"
        task.authorized.add(authenticated)
        task.status = TaskStatus.DONE
        taskRepository.findById(task.id) >> Optional.ofNullable(task)

        when: "user modifies task"
        taskService.modify(task, newName, newDescription)

        then: "exception is thrown"
        thrown(InvalidResourceStateException.class)
    }

    //delete
    void "user should delete existing task"() {
        given: "non-existing task"
        userService.getAuthenticatedUser() >> authenticated
        taskRepository.findById(task.id) >> task

        when: "user deletes task"
        taskService.deleteTask(task)

        then: "operation is performed"
    }
}
