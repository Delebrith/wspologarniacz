package com.purplepanda.wspologarniacz.group

import com.purplepanda.wspologarniacz.group.exception.GroupNotFoundException
import com.purplepanda.wspologarniacz.group.exception.InvalidAffiliationStateException
import com.purplepanda.wspologarniacz.group.exception.NotGroupMemberException
import com.purplepanda.wspologarniacz.task.Task
import com.purplepanda.wspologarniacz.user.AuthorityName
import com.purplepanda.wspologarniacz.user.User
import com.purplepanda.wspologarniacz.user.UserService
import com.purplepanda.wspologarniacz.user.exception.UserNotFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import spock.lang.Specification

import java.time.LocalDateTime

class GroupServiceImplSpecification extends Specification {

    //mocked
    private UserService userService
    private GroupRepository groupRepository

    //tested
    private GroupService groupService

    //test data
    private User authenticated
    private User processed
    private Group group
    private Task task

    void setup() {
        userService = Mock(UserService.class)
        groupRepository = Mock(GroupRepository.class)
        groupService = new GroupServiceImpl(userService, groupRepository)

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
                .id(1L)
                .name("task")
                .updateTime(LocalDateTime.now())
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
        groupRepository.findById(group.id) >> Optional.ofNullable(group)

        when: "inviting other user"
        groupService.inviteUser(group.id, processed.id)

        then: "other user is invited"
        group.affiliations.any {a -> a.getUser() == processed && a.state == AffiliationState.PENDING_INVITATION}
    }

    void "non-member should fail to invite user to his group"() {
        given: "not a member of group"
        userService.getAuthenticatedUser() >> authenticated
        userService.getUser(processed.id) >> Optional.ofNullable(processed)
        groupRepository.findById(group.id) >> Optional.ofNullable(group)

        when: "inviting other user"
        groupService.inviteUser(group.id, processed.id)

        then: "exception is thrown"
        thrown(NotGroupMemberException.class)
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
        groupRepository.findById(group.id) >> Optional.ofNullable(group)

        when: "inviting other user"
        groupService.inviteUser(group.id, processed.id)

        then: "exception is thrown"
        thrown(InvalidAffiliationStateException.class)
    }


    void "member should fail to invite user to non-existing group"() {
        given: "non existing group"
        userService.getAuthenticatedUser() >> authenticated
        userService.getUser(processed.id) >> Optional.ofNullable(processed)
        groupRepository.findById(group.id) >> Optional.empty()

        when: "inviting other user"
        groupService.inviteUser(group.id, processed.id)

        then: "exception is thrown"
        thrown(GroupNotFoundException.class)
    }


    void "member should fail to invite non-existing user to his group"() {
        given: "member of group and non-existing user"
        userService.getAuthenticatedUser() >> authenticated
        userService.getUser(processed.id) >> Optional.empty()

        when: "inviting other user"
        groupService.inviteUser(group.id, processed.id)

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
        groupRepository.findById(group.id) >> Optional.ofNullable(group)

        when: "accepting other user"
        groupService.acceptUserIntoGroup(group.id, processed.id)

        then: "other user is accepted"
        group.affiliations.any {a -> a.getUser() == processed && a.state == AffiliationState.MEMBER}
        group.tasks.every {task -> task.authorized.contains(processed)}

    }

    void "non-member should fail to accept user to his group"() {
        given: "not a member of group"
        userService.getAuthenticatedUser() >> authenticated
        userService.getUser(processed.id) >> Optional.ofNullable(processed)
        groupRepository.findById(group.id) >> Optional.ofNullable(group)

        when: "accepting other user"
        groupService.acceptUserIntoGroup(group.id, processed.id)

        then: "exception is thrown"
        thrown(NotGroupMemberException.class)
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
        groupRepository.findById(group.id) >> Optional.ofNullable(group)

        when: "accepting other user"
        groupService.acceptUserIntoGroup(group.id, processed.id)

        then: "exception is thrown"
        thrown(InvalidAffiliationStateException.class)
    }


    void "member should fail to accept user to non-existing group"() {
        given: "non-existing group"
        userService.getAuthenticatedUser() >> authenticated
        userService.getUser(processed.id) >> Optional.ofNullable(processed)
        groupRepository.findById(group.id) >> Optional.empty()

        when: "accepting other user"
        groupService.acceptUserIntoGroup(group.id, processed.id)

        then: "exception is thrown"
        thrown(GroupNotFoundException.class)
    }


    void "member should fail to accept non-existing user to his group"() {
        given: "member of group and non-existing user"
        userService.getAuthenticatedUser() >> authenticated
        userService.getUser(processed.id) >> Optional.empty()

        when: "accepting other user"
        groupService.acceptUserIntoGroup(group.id, processed.id)

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
        groupRepository.findById(group.id) >> Optional.ofNullable(group)

        when: "rejecting other user"
        groupService.rejectUserFromGroup(group.id, processed.id)

        then: "user is rejected"
        group.affiliations.every {affiliation -> affiliation.getUser() != processed}
    }

    void "non-member should fail to reject user from his group"() {
        given: "not a member of group"
        userService.getAuthenticatedUser() >> authenticated
        userService.getUser(processed.id) >> Optional.ofNullable(processed)
        groupRepository.findById(group.id) >> Optional.ofNullable(group)

        when: "rejecting other user"
        groupService.rejectUserFromGroup(group.id, processed.id)

        then: "exception is thrown"
        thrown(NotGroupMemberException.class)
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
        groupRepository.findById(group.id) >> Optional.ofNullable(group)

        when: "rejecting other user"
        groupService.rejectUserFromGroup(group.id, processed.id)

        then: "exception is thrown"
        thrown(InvalidAffiliationStateException.class)
    }


    void "member should fail to reject user to non-existing group"() {
        given: "non-existing group"
        userService.getAuthenticatedUser() >> authenticated
        userService.getUser(processed.id) >> Optional.ofNullable(processed)
        groupRepository.findById(group.id) >> Optional.empty()

        when: "rejecting other user"
        groupService.rejectUserFromGroup(group.id, processed.id)

        then: "exception is thrown"
        thrown(GroupNotFoundException.class)
    }


    void "member should fail to reject non-existing user to his group"() {
        given: "member of group and non-existing user"
        userService.getAuthenticatedUser() >> authenticated
        userService.getUser(processed.id) >> Optional.empty()

        when: "rejecting other user"
        groupService.rejectUserFromGroup(group.id, processed.id)

        then: "exception is thrown"
        thrown(UserNotFoundException.class)
    }

    //join
    void "non-member should successfully request to join a group"() {
        given: "not a member"
        userService.getAuthenticatedUser() >> authenticated
        groupRepository.findById(group.id) >> Optional.ofNullable(group)

        when: "joining group"
        groupService.joinGroup(group.id)

        then: "affiliation is made"
        group.affiliations.any {
            affiliation -> affiliation.user == authenticated && affiliation.state == AffiliationState.WAITING_FOR_ACCEPTANCE}
    }

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
        groupRepository.findById(group.id) >> Optional.ofNullable(group)

        when: "joining group"
        groupService.joinGroup(group.id)

        then: "exception is thrown"
        thrown(InvalidAffiliationStateException.class)
    }

    void "user should fail to request to join a non-existing group"() {
        given: "non-existing group"
        userService.getAuthenticatedUser() >> authenticated
        groupRepository.findById(group.id) >> Optional.empty()

        when: "joining group"
        groupService.joinGroup(group.id)

        then: "exception is thrown"
        thrown(GroupNotFoundException.class)
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
        groupRepository.findById(group.id) >> Optional.ofNullable(group)

        when: "joining group"
        groupService.acceptInvitation(group.id)

        then: "user becomes member"
        group.affiliations.any {
            affiliation -> affiliation.user == authenticated && affiliation.state == AffiliationState.MEMBER}
        group.tasks.every {task -> task.authorized.contains(authenticated)}
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
        groupRepository.findById(group.id) >> Optional.ofNullable(group)

        when: "joining group"
        groupService.acceptInvitation(group.id)

        then: "exception is thrown"
        thrown(InvalidAffiliationStateException.class)
    }

    void "invited user should fail to accept invitation to a non-existing group"() {
        given: "non-existing group"
        userService.getAuthenticatedUser() >> authenticated
        groupRepository.findById(group.id) >> Optional.empty()

        when: "accepting invitation"
        groupService.acceptInvitation(group.id)

        then: "exception is thrown"
        thrown(GroupNotFoundException.class)
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
        groupRepository.findById(group.id) >> Optional.ofNullable(group)

        when: "rejecting invitation"
        groupService.rejectInvitation(group.id)

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
        groupRepository.findById(group.id) >> Optional.ofNullable(group)

        when: "rejecting invitation"
        groupService.rejectInvitation(group.id)

        then: "exception is thrown"
        thrown(InvalidAffiliationStateException.class)
    }

    void "invited user should fail to reject invitation to a non-existing group"() {
        given: "non-existing group"
        userService.getAuthenticatedUser() >> authenticated
        groupRepository.findById(group.id) >> Optional.empty()

        when: "rejecting invitation"
        groupService.rejectInvitation(group.id)

        then: "exception is thrown"
        thrown(GroupNotFoundException.class)
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
        groupRepository.findById(group.id) >> Optional.ofNullable(group)

        when: "leaving group"
        groupService.leaveGroup(group.id)

        then: "affiliation is deleted"
        group.affiliations.every {affiliation -> affiliation.user != authenticated}
        group.tasks.every {task -> !task.authorized.contains(authenticated)}
    }

    void "non-affiliated user should fail to leave a group"() {
        given: "non-affiliated user"
        userService.getAuthenticatedUser() >> authenticated
        groupRepository.findById(group.id) >> Optional.ofNullable(group)

        when: "leaving group"
        groupService.leaveGroup(group.id)

        then: "exception is thrown"
        thrown(InvalidAffiliationStateException.class)
    }

    void "affiliated user should fail to leave a non-existing group"() {
        given: "non-existing group"
        userService.getAuthenticatedUser() >> authenticated
        groupRepository.findById(group.id) >> Optional.empty()

        when: "leaving group"
        groupService.leaveGroup(group.id)

        then: "exception is thrown"
        thrown(GroupNotFoundException.class)
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
        groupRepository.findById(group.id) >> Optional.ofNullable(group)
        groupRepository.save(group) >> group

        when: "user adds new task to a tasklist"
        Group result = groupService.createTask(group.id, "name", "description")

        then: "task is created"
        !result.tasks.isEmpty()
    }

    void "member should fail to add task for a non-existing group"() {
        given: "non-existing group"
        userService.getAuthenticatedUser() >> authenticated
        groupRepository.findById(group.id) >> Optional.empty()

        when: "user adds task"
        Group result = groupService.createTask(group.id, "name", "description")

        then: "exception is thrown"
        thrown(GroupNotFoundException.class)
    }

    void "not a member should fail to add task for a group"() {
        given: "non-existing group"
        userService.getAuthenticatedUser() >> authenticated
        groupRepository.findById(group.id) >> Optional.ofNullable(group)

        when: "user adds task"
        Group result = groupService.createTask(group.id, "name", "description")

        then: "exception is thrown"
        thrown(NotGroupMemberException.class)
    }
}
