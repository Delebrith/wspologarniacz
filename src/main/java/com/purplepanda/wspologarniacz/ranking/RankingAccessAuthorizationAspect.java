package com.purplepanda.wspologarniacz.ranking;

import com.purplepanda.wspologarniacz.user.UserService;
import com.purplepanda.wspologarniacz.user.authorization.UnauthorizedResourceAccessException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class RankingAccessAuthorizationAspect {

    private final UserService userServiceImpl;

    public RankingAccessAuthorizationAspect(UserService userServiceImpl) {
        this.userServiceImpl = userServiceImpl;
    }

    @Before(value =
            "@annotation(com.purplepanda.wspologarniacz.user.authorization.ResourceModificationAuthorization)" +
            " && args(ranking,..)")
    public void checkModificationAccessRights(JoinPoint joinPoint, Ranking ranking) {
        if (ranking.getAuthorized().stream()
                .noneMatch(a -> a.equals(userServiceImpl.getAuthenticatedUser()))){
            throw new UnauthorizedResourceAccessException();
        }
    }

    @AfterReturning(value =
            "@annotation(com.purplepanda.wspologarniacz.user.authorization.ResourceAccessAuthorization)",
            returning = "ranking")
    public void checkReadAccessRights(JoinPoint joinPoint, Ranking ranking) {
        if (ranking.getAuthorized()
                .stream()
                .noneMatch(u -> u.equals(userServiceImpl.getAuthenticatedUser()))){
            throw new UnauthorizedResourceAccessException();
        }
    }
}
