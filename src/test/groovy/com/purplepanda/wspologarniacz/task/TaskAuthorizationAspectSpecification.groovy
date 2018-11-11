package com.purplepanda.wspologarniacz.task

import com.purplepanda.wspologarniacz.base.config.web.UnauthorizedResourceModificationException
import com.purplepanda.wspologarniacz.user.User
import com.purplepanda.wspologarniacz.user.UserService
import org.aspectj.lang.JoinPoint
import spock.lang.Specification

class TaskAuthorizationAspectSpecification extends Specification {

    //Mocked
    UserService userService
    JoinPoint joinPoint

    //tested
    TaskAuthorizationAspect taskAuthorizationAspect

    //test data
    User authenticated
    Task task

    void setup() {
        userService = Mock(UserService.class)
        joinPoint = Mock(JoinPoint.class)

        authenticated = User.builder()
                .id(1L)
                .name("user")
                .email("user@email.com")
                .build()

        task = Task.builder()
            .name("task")
            .description("task")
            .build()

        taskAuthorizationAspect = new TaskAuthorizationAspect(userService)
    }

    void "unauthorized user should cause exception"() {
        given: "unauthorized user"
        userService.getAuthenticatedUser() >> authenticated
        task.authorized = Collections.emptySet()
        when: "access rights are checked"
        taskAuthorizationAspect.checkAccessRights(joinPoint, task)
        then: "exception is thrown"
        thrown(UnauthorizedResourceModificationException.class)
    }

    void "authorized user should not cause exception"() {
        given: "authorized user"
        userService.getAuthenticatedUser() >> authenticated
        task.authorized = Collections.singletonList(authenticated).toSet()
        when: "access rights are checked"
        taskAuthorizationAspect.checkAccessRights(joinPoint, task)
        then: "no exception is thrown"
    }
}
