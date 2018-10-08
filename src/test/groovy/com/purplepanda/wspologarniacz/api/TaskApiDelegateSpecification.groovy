package com.purplepanda.wspologarniacz.api

import com.purplepanda.wspologarniacz.api.model.TaskInfoDto
import com.purplepanda.wspologarniacz.base.config.web.InvalidResourceStateException
import com.purplepanda.wspologarniacz.base.config.web.UnauthorizedResourceModificationException
import com.purplepanda.wspologarniacz.task.Task
import com.purplepanda.wspologarniacz.task.TaskNotFoundException
import com.purplepanda.wspologarniacz.task.TaskService
import com.purplepanda.wspologarniacz.task.TaskStatus
import com.purplepanda.wspologarniacz.user.AuthorityName
import com.purplepanda.wspologarniacz.user.User
import org.springframework.http.ResponseEntity
import spock.lang.Specification

import java.time.LocalDateTime

class TaskApiDelegateSpecification extends Specification {

    //mocked
    private TaskService taskService

    //testes
    private TaskApiDelegate taskApiDelegate

    //test data
    private User authenticated
    private Task task
    private TaskInfoDto taskInfoDto

    void setup() {
        taskService = Mock(TaskService.class)
        taskApiDelegate = new TaskApiDelegateImpl(taskService)

        authenticated = User.builder()
            .id(1L)
            .name("user")
            .active(true)
            .authorities(Collections.singletonList(AuthorityName.USER))
            .build()

        task = Task.builder()
            .id(1L)
            .name("task")
            .status(TaskStatus.ADDED)
            .updateTime(LocalDateTime.now())
            .authorized(Collections.singletonList(authenticated).toSet())
            .build()

        taskInfoDto = new TaskInfoDto()
            .name("task")
    }

    //mark as done
    void "authorized user should successfully mark task as done"() {
        given: "task for authenticated user"
        taskService.markAsDone(task.id) >> task

        when: "user marks task as done"
        ResponseEntity responseEntity = taskApiDelegate.markTaskAsDone(task.id)

        then: "response is success"
        responseEntity.statusCode.'2xxSuccessful'
    }

    void "unauthorized user should fail to mark task as done"() {
        given: "user not authorized to modify the task"
        taskService.markAsDone(task.id) >> { throw new UnauthorizedResourceModificationException() }

        when: "user marks task as done"
        ResponseEntity responseEntity = taskApiDelegate.markTaskAsDone(task.id)

        then: "exception is thrown"
        thrown(UnauthorizedResourceModificationException.class)
    }

    void "user should fail to mark non-existing task as done"() {
        given: "non-existing task"
        taskService.markAsDone(task.id) >> { throw new TaskNotFoundException() }

        when: "user marks task as done"
        ResponseEntity responseEntity = taskApiDelegate.markTaskAsDone(task.id)

        then: "exception is thrown"
        thrown(TaskNotFoundException.class)
    }

    void "user should fail to mark done task as done"() {
        given: "non-existing task"
        taskService.markAsDone(task.id) >> { throw new InvalidResourceStateException() }

        when: "user marks task as done"
        ResponseEntity responseEntity = taskApiDelegate.markTaskAsDone(task.id)

        then: "exception is thrown"
        thrown(InvalidResourceStateException.class)
    }

    //modify
    void "authorized user should successfully modify task"() {
        given: "task for authenticated user"
        taskService.modify(task.id, taskInfoDto.name, taskInfoDto.description) >> task

        when: "user modifies task"
        ResponseEntity responseEntity = taskApiDelegate.modify(task.id, taskInfoDto)

        then: "response is success"
        responseEntity.statusCode.'2xxSuccessful'
    }

    void "unauthorized user should fail to modify task"() {
        given: "user not authorized to modify the task"
        taskService.modify(task.id, taskInfoDto.name, taskInfoDto.description) >>
                { throw new UnauthorizedResourceModificationException() }

        when: "user modifies task"
        ResponseEntity responseEntity = taskApiDelegate.modify(task.id, taskInfoDto)

        then: "exception is thrown"
        thrown(UnauthorizedResourceModificationException.class)
    }

    void "user should fail to modify non-existing task"() {
        given: "non-existing task"
        taskService.modify(task.id, taskInfoDto.name, taskInfoDto.description) >> { throw new TaskNotFoundException() }

        when: "user marks task as done"
        ResponseEntity responseEntity = taskApiDelegate.modify(task.id, taskInfoDto)

        then: "exception is thrown"
        thrown(TaskNotFoundException.class)
    }

    void "user should fail to modify done task"() {
        given: "non-existing task"
        taskService.modify(task.id, taskInfoDto.name, taskInfoDto.description) >> { throw new InvalidResourceStateException() }

        when: "user marks task as done"
        ResponseEntity responseEntity = taskApiDelegate.modify(task.id, taskInfoDto)

        then: "exception is thrown"
        thrown(InvalidResourceStateException.class)
    }

    //delete
    void "authorized user should successfully delete task"() {
        given: "task for authenticated user"

        when: "user modifies task"
        ResponseEntity responseEntity = taskApiDelegate.deleteTask(task.id)

        then: "response is success"
        responseEntity.statusCode.'2xxSuccessful'
    }

    void "unauthorized user should fail to delete task"() {
        given: "user not authorized to modify the task"
        taskService.deleteTask(task.id) >> { throw new UnauthorizedResourceModificationException() }

        when: "user modifies task"
        ResponseEntity responseEntity = taskApiDelegate.deleteTask(task.id)

        then: "exception is thrown"
        thrown(UnauthorizedResourceModificationException.class)
    }

    void "user should fail to delete non-existing task"() {
        given: "non-existing task"
        taskService.deleteTask(task.id) >> { throw new TaskNotFoundException() }

        when: "user marks task as done"
        ResponseEntity responseEntity = taskApiDelegate.deleteTask(task.id)

        then: "exception is thrown"
        thrown(TaskNotFoundException.class)
    }
}
