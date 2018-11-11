package com.purplepanda.wspologarniacz.group.authorization;

import com.purplepanda.wspologarniacz.group.AffiliationState;
import com.purplepanda.wspologarniacz.group.Group;
import com.purplepanda.wspologarniacz.group.exception.NotGroupMemberException;
import com.purplepanda.wspologarniacz.user.User;
import com.purplepanda.wspologarniacz.user.UserService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class GroupMemberAuthorizationAspect {

    private final UserService userService;

    @Autowired
    public GroupMemberAuthorizationAspect(UserService userService) {
        this.userService = userService;
    }

    @Before(value = "@annotation(com.purplepanda.wspologarniacz.group.authorization.GroupMemberAccess) && args(group,..)")
    public void checkMembership(JoinPoint joinPoint, Group group){
        User authenticated = userService.getAuthenticatedUser();
        if (group.getAffiliations().stream()
                .filter(a -> a.getState().equals(AffiliationState.MEMBER))
                .map(a -> a.getUser())
                .noneMatch(u -> u.equals(authenticated))) {
            throw new NotGroupMemberException();
        }
    }
}
