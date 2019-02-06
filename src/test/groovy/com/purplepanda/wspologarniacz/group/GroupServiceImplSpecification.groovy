package com.purplepanda.wspologarniacz.group

import com.purplepanda.wspologarniacz.group.exception.GroupNotFoundException
import com.purplepanda.wspologarniacz.group.exception.InvalidAffiliationStateException
import com.purplepanda.wspologarniacz.group.exception.NotGroupMemberException
import com.purplepanda.wspologarniacz.ranking.Category
import com.purplepanda.wspologarniacz.ranking.Ranking
import com.purplepanda.wspologarniacz.ranking.Score
import com.purplepanda.wspologarniacz.schedule.Ordinal
import com.purplepanda.wspologarniacz.schedule.Schedule
import com.purplepanda.wspologarniacz.task.Task
import com.purplepanda.wspologarniacz.user.AuthorityName
import com.purplepanda.wspologarniacz.user.User
import com.purplepanda.wspologarniacz.user.UserService
import com.purplepanda.wspologarniacz.user.exception.UserNotFoundException
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import spock.lang.Specification

import java.time.LocalDateTime
import java.time.Period

class GroupServiceImplSpecification extends Specification {

    //mocked
    private UserService userService
    private GroupRepository groupRepository
    private ApplicationEventPublisher eventPublisher

    //tested
    private GroupService groupService

    //test data
    private User authenticated
    private User processed
    private Group group
    private Task task
    private Ranking ranking
    private Schedule schedule

    void setup() {
        eventPublisher = Mock(ApplicationEventPublisher.class)
        userService = Mock(UserService.class)
        groupRepository = Mock(GroupRepository.class)
        groupService = new GroupServiceImpl(userService, groupRepository, eventPublisher)

        authenticated = User.builder()
                .id(1L)
                .name("user1")
                .email("user1@mail.com")
                .active(true)
                .authorities(Collections.singletonList(AuthorityName.USER))
                .build()

        processed = User.builder()
                .id(2L)
                .name("user2")
                .email("user2@mail.com")
                .active(true)
                .authorities(Collections.singletonList(AuthorityName.USER))
                .build()

        group = Group.builder()
                .id(1L)
                .name("group")
                .build()

        task = Task.builder()
                .name("task")
                .updateTime(LocalDateTime.now())
                .build()
        task.id = 1L

        Score score = Score.builder()
                        .id(1L)
                        .user(authenticated)
                        .points(1)
                        .build()

        Category category = Category.builder()
                                .id(1L)
                                .name("category")
                                .scores(Collections.singletonList(score).toSet())
                                .build()

        ranking = Ranking.builder()
                    .name("ranking")
                    .categories(Collections.singletonList(category).toSet())
                    .build()

        Ordinal ordinal = Ordinal.builder()
                            .id(1L)
                            .users(Collections.singletonList(authenticated).toSet())
                            .build()

        schedule = Schedule.builder()
                    .name("schedule")
                    .counter(1)
                    .period(Period.ofDays(7))
                    .order(Collections.singletonList(ordinal).toSet())
                    .build()
    }

    void "authenticated user should get info on his groups"() {
        given: "authenticated user with groups"
        group.getAffiliations().add(Affiliation.builder()
                .id(1L)
                .user(authenticated)
                .state(AffiliationState.MEMBER)
                .lastUpdated(LocalDateTime.now())
                .build()
        )
        userService.getAuthenticatedUser() >> authenticated
        groupRepository.findByAffiliationsUser(authenticated) >> Collections.singletonList(group)

        when: "user requests his groups info"
        List<Group> result = groupService.getAuthenticatedUsersGroups()

        then: "group list is successfully returned"
        result.size() == 1
    }

    void "unauthenticated user should get an exception when requesting info on his groups"() {
        given: "unauthenticated user"
        userService.getAuthenticatedUser() >> { throw new IllegalStateException() }

        when: "user requests hid groups info"
        groupService.getAuthenticatedUsersGroups()

        then: "exception is thrown"
        thrown(IllegalStateException.class)
    }

    void "user should get requested pageable when searching for groups"() {
        given: "requested pageable and name"
        int number = 0, size = 2
        Pageable pageable = PageRequest.of(number, size)
        String name = "gro"

        and: "groups"
        groupRepository.findByNameIgnoreCaseContaining(name, pageable) >>
                new PageImpl<Group>(Collections.singletonList(group))

        when: "groups are searched"
        Page<Group> result = groupService.searchGroupsByName(name, pageable)

        then: "result is successfully returned"
        !result.asList().isEmpty()
        result.asList().size() <= size
    }

    void "user should get existing group info"() {
        given: "requested group"
        groupRepository.findById(group.id) >> Optional.ofNullable(group)

        when: "group info is requested"
        Group result = groupService.getGroup(group.id)

        then: "result is successfully returned"
        result != null
        result == group
    }

    void "user should get exception when requesting non-existing group info"() {
        given: "requested group"
        groupRepository.findById(group.id) >> Optional.empty()

        when: "group info is requested"
        groupService.getGroup(group.id)

        then: "exception is thrown"
        thrown(GroupNotFoundException.class)
    }

    void "user should create group successfully"() {
        given: "group info and authenticated user"
        userService.getAuthenticatedUser() >> authenticated
        groupRepository.save(group) >> group

        when: "group is created"
        Group created = groupService.createGroup(group)

        then: "group id is accessible"
        created.id != null

        and: "authenticated user is a member"
        created.affiliations.any {a -> a.getUser() == authenticated}
    }

    //inivite
    void "member should successfully invite user to his group"() {
        given: "authenticated user as member of group"
        group.getAffiliations().add(Affiliation.builder()
                .id(1L)
                .user(authenticated)
                .state(AffiliationState.MEMBER)
                .lastUpdated(LocalDateTime.now())
                .build()
        )
        userService.getAuthenticatedUser() >> authenticated
        userService.getUser(processed.id) >> Optional.ofNullable(processed)

        when: "inviting other user"
        groupService.inviteUser(group, processed.id)

        then: "other user is invited"
        group.affiliations.any {a -> a.getUser() == processed && a.state == AffiliationState.PENDING_INVITATION}
    }


    void "member should fail to invite user affiliated to his group"() {
        given: "member of group and already affiliated user"
        group.getAffiliations().add(Affiliation.builder()
                .id(1L)
                .user(authenticated)
                .state(AffiliationState.MEMBER)
                .lastUpdated(LocalDateTime.now())
                .build()
        )
        group.getAffiliations().add(Affiliation.builder()
                .id(2L)
                .user(processed)
                .state(AffiliationState.MEMBER)
                .lastUpdated(LocalDateTime.now())
                .build()
        )
        userService.getAuthenticatedUser() >> authenticated
        userService.getUser(processed.id) >> Optional.ofNullable(processed)

        when: "inviting other user"
        groupService.inviteUser(group, processed.id)

        then: "exception is thrown"
        thrown(InvalidAffiliationStateException.class)
    }

    void "member should fail to invite non-existing user to his group"() {
        given: "member of group and non-existing user"
        userService.getAuthenticatedUser() >> authenticated
        userService.getUser(processed.id) >> Optional.empty()

        when: "inviting other user"
        groupService.inviteUser(group, processed.id)

        then: "exception is thrown"
        thrown(UserNotFoundException.class)
    }

    //accept user
    void "member should successfully accept user to his group"() {
        given: "authenticated user as member of group"
        group.getAffiliations().add(Affiliation.builder()
                .id(1L)
                .user(authenticated)
                .state(AffiliationState.MEMBER)
                .lastUpdated(LocalDateTime.now())
                .build()
        )
        group.getAffiliations().add(Affiliation.builder()
                .id(2L)
                .user(processed)
                .state(AffiliationState.WAITING_FOR_ACCEPTANCE)
                .lastUpdated(LocalDateTime.now())
                .build()
        )
        userService.getAuthenticatedUser() >> authenticated
        userService.getUser(processed.id) >> Optional.ofNullable(processed)

        when: "accepting other user"
        groupService.acceptUserIntoGroup(group, processed.id)

        then: "other user is accepted"
        group.affiliations.any {a -> a.getUser() == processed && a.state == AffiliationState.MEMBER}

    }


    void "member should fail to accept user affiliated to his group"() {
        given: "member of group and user not waiting for acceptance"
        group.getAffiliations().add(Affiliation.builder()
                .id(1L)
                .user(authenticated)
                .state(AffiliationState.MEMBER)
                .lastUpdated(LocalDateTime.now())
                .build()
        )
        group.getAffiliations().add(Affiliation.builder()
                .id(2L)
                .user(processed)
                .state(AffiliationState.MEMBER)
                .lastUpdated(LocalDateTime.now())
                .build()
        )
        userService.getAuthenticatedUser() >> authenticated
        userService.getUser(processed.id) >> Optional.ofNullable(processed)

        when: "accepting other user"
        groupService.acceptUserIntoGroup(group, processed.id)

        then: "exception is thrown"
        thrown(InvalidAffiliationStateException.class)
    }


    void "member should fail to accept non-existing user to his group"() {
        given: "member of group and non-existing user"
        userService.getAuthenticatedUser() >> authenticated
        userService.getUser(processed.id) >> Optional.empty()

        when: "accepting other user"
        groupService.acceptUserIntoGroup(group, processed.id)

        then: "exception is thrown"
        thrown(UserNotFoundException.class)
    }

    //reject user
    void "member should successfully reject user from his group"() {
        given: "authenticated user as member of group"
        group.getAffiliations().add(Affiliation.builder()
                .id(1L)
                .user(authenticated)
                .state(AffiliationState.MEMBER)
                .lastUpdated(LocalDateTime.now())
                .build()
        )
        group.getAffiliations().add(Affiliation.builder()
                .id(2L)
                .user(processed)
                .state(AffiliationState.WAITING_FOR_ACCEPTANCE)
                .lastUpdated(LocalDateTime.now())
                .build()
        )
        userService.getAuthenticatedUser() >> authenticated
        userService.getUser(processed.id) >> Optional.ofNullable(processed)

        when: "rejecting other user"
        groupService.rejectUserFromGroup(group, processed.id)

        then: "user is rejected"
        group.affiliations.every {affiliation -> affiliation.getUser() != processed}
    }


    void "member should fail to reject user not waiting for acceptance to his group"() {
        given: "member of group and user not waiting for acceptance"
        group.getAffiliations().add(Affiliation.builder()
                .id(1L)
                .user(authenticated)
                .state(AffiliationState.MEMBER)
                .lastUpdated(LocalDateTime.now())
                .build()
        )
        group.getAffiliations().add(Affiliation.builder()
                .id(2L)
                .user(processed)
                .state(AffiliationState.MEMBER)
                .lastUpdated(LocalDateTime.now())
                .build()
        )
        userService.getAuthenticatedUser() >> authenticated
        userService.getUser(processed.id) >> Optional.ofNullable(processed)

        when: "rejecting other user"
        groupService.rejectUserFromGroup(group, processed.id)

        then: "exception is thrown"
        thrown(InvalidAffiliationStateException.class)
    }


    void "member should fail to reject non-existing user to his group"() {
        given: "member of group and non-existing user"
        userService.getAuthenticatedUser() >> authenticated
        userService.getUser(processed.id) >> Optional.empty()

        when: "rejecting other user"
        groupService.rejectUserFromGroup(group, processed.id)

        then: "exception is thrown"
        thrown(UserNotFoundException.class)
    }

    //join

    void "affiliated user should fail to request to join a group"() {
        given: "affiliated user"
        group.getAffiliations().add(Affiliation.builder()
                .id(1L)
                .user(authenticated)
                .state(AffiliationState.MEMBER)
                .lastUpdated(LocalDateTime.now())
                .build()
        )
        userService.getAuthenticatedUser() >> authenticated

        when: "joining group"
        groupService.joinGroup(group)

        then: "exception is thrown"
        thrown(InvalidAffiliationStateException.class)
    }


    //accept invitation
    void "invited user should successfully accept invitation to a group"() {
        given: "invited user"
        group.getAffiliations().add(Affiliation.builder()
                .id(1L)
                .user(authenticated)
                .state(AffiliationState.PENDING_INVITATION)
                .lastUpdated(LocalDateTime.now())
                .build()
        )
        userService.getAuthenticatedUser() >> authenticated

        when: "joining group"
        groupService.acceptInvitation(group)

        then: "user becomes member"
        group.affiliations.any {
            affiliation -> affiliation.user == authenticated && affiliation.state == AffiliationState.MEMBER}
    }

    void "not invited user should fail accept invitation to a group"() {
        given: "not invited user"
        group.getAffiliations().add(Affiliation.builder()
                .id(1L)
                .user(authenticated)
                .state(AffiliationState.WAITING_FOR_ACCEPTANCE)
                .lastUpdated(LocalDateTime.now())
                .build()
        )
        userService.getAuthenticatedUser() >> authenticated

        when: "joining group"
        groupService.acceptInvitation(group)

        then: "exception is thrown"
        thrown(InvalidAffiliationStateException.class)
    }


    //reject invitation
    void "invited user should successfully reject invitation to a group"() {
        given: "invited user"
        group.getAffiliations().add(Affiliation.builder()
                .id(1L)
                .user(authenticated)
                .state(AffiliationState.PENDING_INVITATION)
                .lastUpdated(LocalDateTime.now())
                .build()
        )
        userService.getAuthenticatedUser() >> authenticated

        when: "rejecting invitation"
        groupService.rejectInvitation(group)

        then: "affiliation is deleted"
        group.affiliations.every {affiliation -> affiliation.user != authenticated}
    }

    void "not invited user should fail reject invitation to a group"() {
        given: "not invited user"
        group.getAffiliations().add(Affiliation.builder()
                .id(1L)
                .user(authenticated)
                .state(AffiliationState.WAITING_FOR_ACCEPTANCE)
                .lastUpdated(LocalDateTime.now())
                .build()
        )
        userService.getAuthenticatedUser() >> authenticated

        when: "rejecting invitation"
        groupService.rejectInvitation(group)

        then: "exception is thrown"
        thrown(InvalidAffiliationStateException.class)
    }


    //leave
    void "affiliated user should successfully leave group"() {
        given: "affiliated user"
        group.getAffiliations().add(Affiliation.builder()
                .id(1L)
                .user(authenticated)
                .state(AffiliationState.MEMBER)
                .lastUpdated(LocalDateTime.now())
                .build()
        )
        userService.getAuthenticatedUser() >> authenticated

        when: "leaving group"
        groupService.leaveGroup(group)

        then: "affiliation is deleted"
        group.affiliations.every {affiliation -> affiliation.user != authenticated}
    }

    void "non-affiliated user should fail to leave a group"() {
        given: "non-affiliated user"
        userService.getAuthenticatedUser() >> authenticated

        when: "leaving group"
        groupService.leaveGroup(group)

        then: "exception is thrown"
        thrown(InvalidAffiliationStateException.class)
    }


    //add tasks
    void "member should successfully add tasks to his group"() {
        given: "member and his group"
        group.getAffiliations().add(Affiliation.builder()
                .id(1L)
                .user(authenticated)
                .state(AffiliationState.MEMBER)
                .lastUpdated(LocalDateTime.now())
                .build()
        )
        userService.getAuthenticatedUser() >> authenticated
        groupRepository.save(group) >> group

        when: "user adds new task to a tasklist"
        Group result = groupService.createTask(group, task)

        then: "task is created"
        !result.tasks.isEmpty()
    }

    //add ranking
    void "member should successfully add ranking to his group"() {
        given: "member and his group"
        group.getAffiliations().add(Affiliation.builder()
                .id(1L)
                .user(authenticated)
                .state(AffiliationState.MEMBER)
                .lastUpdated(LocalDateTime.now())
                .build()
        )
        userService.getAuthenticatedUser() >> authenticated
        groupRepository.save(group) >> group

        when: "user adds new task to a tasklist"
        Group result = groupService.createRanking(group, ranking)

        then: "task is created"
        !result.rankings.isEmpty()
    }

    //add tasks
    void "member should successfully add schedule to his group"() {
        given: "member and his group"
        group.getAffiliations().add(Affiliation.builder()
                .id(1L)
                .user(authenticated)
                .state(AffiliationState.MEMBER)
                .lastUpdated(LocalDateTime.now())
                .build()
        )
        userService.getAuthenticatedUser() >> authenticated
        groupRepository.save(group) >> group

        when: "user adds new task to a tasklist"
        Group result = groupService.createSchedule(group, schedule)

        then: "task is created"
        !result.schedules.isEmpty()
    }

}
