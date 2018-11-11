package com.purplepanda.wspologarniacz.task;

import com.purplepanda.wspologarniacz.base.config.web.UnauthorizedResourceModificationException;
import com.purplepanda.wspologarniacz.user.User;
import com.purplepanda.wspologarniacz.user.UserService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TaskAuthorizationAspect {

    private final UserService userService;

    @Autowired
    public TaskAuthorizationAspect(UserService userService) {
        this.userService = userService;
    }

    @Before(value = "@annotation(com.purplepanda.wspologarniacz.user.authorization.ResourceAccessAuthorization) && args(task,..)")
    public void checkAccessRights(JoinPoint joinPoint, Task task) {
        User authenticated = userService.getAuthenticatedUser();
        if (task.getAuthorized()
                .stream()
                .noneMatch(u -> u.equals(authenticated))){
            throw new UnauthorizedResourceModificationException();
        }
    }
}
