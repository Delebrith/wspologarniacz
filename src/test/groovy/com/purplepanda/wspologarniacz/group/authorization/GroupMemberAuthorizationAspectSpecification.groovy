package com.purplepanda.wspologarniacz.group.authorization

import com.purplepanda.wspologarniacz.group.Affiliation
import com.purplepanda.wspologarniacz.group.AffiliationState
import com.purplepanda.wspologarniacz.group.Group
import com.purplepanda.wspologarniacz.group.exception.NotGroupMemberException
import com.purplepanda.wspologarniacz.user.User
import com.purplepanda.wspologarniacz.user.UserService
import org.aspectj.lang.JoinPoint
import spock.lang.Specification

class GroupMemberAuthorizationAspectSpecification extends Specification {

    //mocked
    private UserService userService
    private JoinPoint joinPoint

    //tested
    private GroupMemberAuthorizationAspect groupMemberAuthorizationAspect

    //test data
    private Group group
    private User authenticated
    private Affiliation affiliation

    void setup() {
        joinPoint = Mock(JoinPoint.class)
        userService = Mock(UserService.class)
        groupMemberAuthorizationAspect = new GroupMemberAuthorizationAspect(userService)

        authenticated = User.builder()
            .id(1L)
            .name("user")
            .email("user@email.com")
            .build()

        group = Group.builder()
            .id(1L)
            .name("group")
            .build()
    }

    void "no affiliation should cause exception"() {
        given: "not affiliated user"
        userService.getAuthenticatedUser() >> authenticated
        group.affiliations = Collections.emptySet()

        when: "membership is checked"
        groupMemberAuthorizationAspect.checkMembership(joinPoint, group)

        then: "exception is thrown"
        thrown(NotGroupMemberException.class)
    }

    void "non-member affiliation should cause exception"() {
        given: "not-a-member user"
        userService.getAuthenticatedUser() >> authenticated
        affiliation = Affiliation.builder()
                .id(1L)
                .user(authenticated)
                .state(AffiliationState.WAITING_FOR_ACCEPTANCE)
                .build()
        group.affiliations = Collections.singletonList(affiliation).toSet()

        when: "membership is checked"
        groupMemberAuthorizationAspect.checkMembership(joinPoint, group)

        then: "exception is thrown"
        thrown(NotGroupMemberException.class)
    }

    void "member affiliation should not cause exception"() {
        given: "not affiliated user"
        userService.getAuthenticatedUser() >> authenticated
        affiliation = Affiliation.builder()
                .id(1L)
                .user(authenticated)
                .state(AffiliationState.MEMBER)
                .build()
        group.affiliations = Collections.singletonList(affiliation).toSet()

        when: "membership is checked"
        groupMemberAuthorizationAspect.checkMembership(joinPoint, group)

        then: "no exception is thrown"
    }
}
