package com.purplepanda.wspologarniacz.ranking

import com.purplepanda.wspologarniacz.user.User
import com.purplepanda.wspologarniacz.user.UserService
import com.purplepanda.wspologarniacz.user.authorization.UnauthorizedResourceAccessException
import org.aspectj.lang.JoinPoint
import spock.lang.Specification

class RankingAccessAuthorizationAspectSpecification extends Specification {

    //mocked
    JoinPoint joinPoint
    UserService userService

    //tested
    RankingAccessAuthorizationAspect rankingAccessAuthorizationAspect

    //test data
    Ranking ranking
    User authenticated

    void setup() {
        authenticated = User.builder()
            .name("user")
            .id(1L)
            .email("user@email.com")
            .build()

        ranking = Ranking.builder()
                .name("ranking")
                .categories(Collections.singletonList(
                Category.builder()
                        .id(1L)
                        .name("category")
                        .build())
                .toSet())
                .build()
        ranking.id = 1L

        userService = Mock(UserService.class)
        joinPoint = Mock(JoinPoint.class)
        rankingAccessAuthorizationAspect = new RankingAccessAuthorizationAspect(userService)
    }


    void "unauthorized user reading should cause exception"() {
        given: "unauthorized user"
        userService.getAuthenticatedUser() >> authenticated
        ranking.authorized = Collections.emptySet()
        when: "access rights are checked"
        rankingAccessAuthorizationAspect.checkReadAccessRights(joinPoint, ranking)
        then: "exception is thrown"
        thrown(UnauthorizedResourceAccessException.class)
    }

    void "authorized user reading should not cause exception"() {
        given: "authorized user"
        userService.getAuthenticatedUser() >> authenticated
        ranking.authorized = Collections.singletonList(authenticated).toSet()
        when: "access rights are checked"
        rankingAccessAuthorizationAspect.checkReadAccessRights(joinPoint, ranking)
        then: "no exception is thrown"
    }


    void "unauthorized user modifying should cause exception"() {
        given: "unauthorized user"
        userService.getAuthenticatedUser() >> authenticated
        ranking.authorized = Collections.emptySet()
        when: "access rights are checked"
        rankingAccessAuthorizationAspect.checkModificationAccessRights(joinPoint, ranking)
        then: "exception is thrown"
        thrown(UnauthorizedResourceAccessException.class)
    }

    void "authorized user modifying should not cause exception"() {
        given: "authorized user"
        userService.getAuthenticatedUser() >> authenticated
        ranking.authorized = Collections.singletonList(authenticated).toSet()
        when: "access rights are checked"
        rankingAccessAuthorizationAspect.checkModificationAccessRights(joinPoint, ranking)
        then: "no exception is thrown"
    }
}
