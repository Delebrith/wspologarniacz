package com.purplepanda.wspologarniacz.api

import com.purplepanda.wspologarniacz.api.model.GroupDto
import com.purplepanda.wspologarniacz.api.model.RankingDto
import com.purplepanda.wspologarniacz.api.model.ScheduleDto
import com.purplepanda.wspologarniacz.api.model.TaskDto
import com.purplepanda.wspologarniacz.api.model.TaskInfoDto
import com.purplepanda.wspologarniacz.group.Affiliation
import com.purplepanda.wspologarniacz.group.AffiliationState
import com.purplepanda.wspologarniacz.group.Group
import com.purplepanda.wspologarniacz.group.GroupMapper
import com.purplepanda.wspologarniacz.group.GroupService
import com.purplepanda.wspologarniacz.group.exception.GroupNotFoundException
import com.purplepanda.wspologarniacz.group.exception.InvalidAffiliationStateException
import com.purplepanda.wspologarniacz.group.exception.NotGroupMemberException
import com.purplepanda.wspologarniacz.ranking.Category
import com.purplepanda.wspologarniacz.ranking.Ranking
import com.purplepanda.wspologarniacz.ranking.RankingMapper
import com.purplepanda.wspologarniacz.schedule.Ordinal
import com.purplepanda.wspologarniacz.schedule.Schedule
import com.purplepanda.wspologarniacz.schedule.ScheduleMapper
import com.purplepanda.wspologarniacz.task.Task
import com.purplepanda.wspologarniacz.task.TaskMapper
import com.purplepanda.wspologarniacz.user.AuthorityName
import com.purplepanda.wspologarniacz.user.User
import com.purplepanda.wspologarniacz.user.UserService
import com.purplepanda.wspologarniacz.user.exception.UserNotFoundException
import org.hibernate.criterion.Order
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import spock.lang.Specification
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Period

class GroupApiDelegateSpecification extends Specification {

    //mocked
    private GroupService groupService
    private UserService userService

    //tested
    private GroupApiDelegate groupApiDelegate

    //test data
    private User authenticated
    private User processed
    private Group group
    private Task task
    private TaskInfoDto taskInfoDto
    private Ranking ranking
    private RankingDto rankingDto
    private Schedule schedule
    private ScheduleDto scheduleDto

    void setup() {
        userService = Mock(UserService.class)
        groupService = Mock(GroupService.class)
        groupApiDelegate = new GroupApiDelegateImpl(groupService, userService)

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

        taskInfoDto = new TaskInfoDto()
                .name("task")

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

        rankingDto = RankingMapper.getInstance().toDto(ranking)

        schedule = Schedule.builder()
                .name("schedule")
                .counter(1)
                .period(Period.ofDays(7))
                .reminderTime(LocalTime.now())
                .order(Collections.singletonList(
                    Ordinal.builder()
                        .id(1L)
                        .users(Collections.singletonList(authenticated).toSet())
                        .build()
                    ).toSet())
                .build()

        scheduleDto = ScheduleMapper.getInstance().toDto(schedule)
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
        groupService.getAuthenticatedUsersGroups() >> Collections.singletonList(group)

        when: "user requests his groups info"
        ResponseEntity<List<GroupDto>> responseEntity = groupApiDelegate.getMyGroups()

        then: "group list is successfully returned"
        responseEntity.statusCode.is2xxSuccessful()
        responseEntity.body.asList().equals(
                Collections.singletonList(GroupMapper.getInstance().toDto(group))
        )
    }

    void "unauthenticated user should get an exception when requesting info on his groups"() {
        given: "unauthenticated user"
        groupService.getAuthenticatedUsersGroups() >> { throw new IllegalStateException() }

        when: "user requests hid groups info"
        ResponseEntity<List<GroupDto>> responseEntity = groupApiDelegate.getMyGroups()

        then: "exception is thrown"
        thrown(IllegalStateException.class)
    }

    void "user should get requested pageable when searching for groups"() {
        given: "requested pageable"
        int number = 0, size = 2
        String name = "gro"

        and: "user with groups"
        group.getAffiliations().add(Affiliation.builder()
                .id(1L)
                .user(authenticated)
                .state(AffiliationState.MEMBER)
                .lastUpdated(LocalDateTime.now())
                .build())

        groupService.searchGroupsByName(name, PageRequest.of(number, size)) >>
                new PageImpl<Group>(Collections.singletonList(group))

        when: "groups are searched"
        ResponseEntity<List<GroupDto>> responseEntity = groupApiDelegate.searchGroups(name, size, number)

        then: "result is successfully returned"
        responseEntity.statusCode.is2xxSuccessful()
        responseEntity.body.asList().size() <= size
    }

    void "user should get existing group info"() {
        given: "requested group"
        groupService.getGroup(group.id) >> group

        when: "group info is requested"
        ResponseEntity<GroupDto> responseEntity = groupApiDelegate.getGroup(group.id)

        then: "result is successfully returned"
        responseEntity.statusCode.is2xxSuccessful()
        responseEntity.body.equals(GroupMapper.getInstance().toDto(group))
    }

    void "user should get exception when requesting non-existing group info"() {
        given: "requested group"
        groupService.getGroup(group.id) >> { throw new GroupNotFoundException()}

        when: "group info is requested"
        ResponseEntity<GroupDto> responseEntity = groupApiDelegate.getGroup(group.id)

        then: "exception is thrown"
        thrown(GroupNotFoundException.class)
    }

    void "user should create group successfully"() {
        given: "group dto"
        GroupDto groupDto = GroupMapper.getInstance().toDto(group)
        groupService.createGroup(_) >> group

        when: "group is created"
        ResponseEntity<Void> responseEntity = groupApiDelegate.createGroup(groupDto)

        then: "response is success"
        responseEntity.statusCode.is2xxSuccessful()
        responseEntity.headers.getLocation() == URI.create("/group/find/" + group.id)
    }

    //inivite
    void "member should successfully invite user to his group"() {
        given: "authenticated user as member of group"

        when: "inviting other user"
        ResponseEntity responseEntity = groupApiDelegate.inviteUserToGroup(group.id, processed.id)

        then: "response is success"
        responseEntity.statusCode.'2xxSuccessful'
    }

    void "non-member should fail to invite user to his group"() {
        given: "not a member of group"
        groupService.getGroup(group.id) >> group
        groupService.inviteUser(group, processed.id) >> { throw new NotGroupMemberException() }

        when: "inviting other user"
        ResponseEntity responseEntity = groupApiDelegate.inviteUserToGroup(group.id, processed.id)

        then: "exception is thrown"
        thrown(NotGroupMemberException.class)
    }


    void "member should fail to invite user affiliated to his group"() {
        given: "member of group and already affiliated user"
        groupService.getGroup(group.id) >> group
        groupService.inviteUser(group, processed.id) >> { throw new InvalidAffiliationStateException() }

        when: "inviting other user"
        ResponseEntity responseEntity = groupApiDelegate.inviteUserToGroup(group.id, processed.id)

        then: "exception is thrown"
        thrown(InvalidAffiliationStateException.class)
    }


    void "member should fail to invite user to non-existing group"() {
        given: "non exisitng group"
        groupService.getGroup(group.id) >> { throw new GroupNotFoundException() }

        when: "inviting other user"
        ResponseEntity responseEntity = groupApiDelegate.inviteUserToGroup(group.id, processed.id)

        then: "exception is thrown"
        thrown(GroupNotFoundException.class)
    }


    void "member should fail to invite non-existing user to his group"() {
        given: "member of group and non-existing user"
        groupService.getGroup(group.id) >> group
        groupService.inviteUser(group, processed.id) >> { throw new UserNotFoundException() }

        when: "inviting other user"
        ResponseEntity responseEntity = groupApiDelegate.inviteUserToGroup(group.id, processed.id)

        then: "exception is thrown"
        thrown(UserNotFoundException.class)
    }

    //accept user
    void "member should successfully accept user to his group"() {
        given: "authenticated user as member of group"

        when: "accepting other user"
        ResponseEntity responseEntity = groupApiDelegate.acceptUserIntoGroup(group.id, processed.id)

        then: "response is success"
        responseEntity.statusCode.'2xxSuccessful'
    }

    void "member should fail to accept user affiliated to his group"() {
        given: "member of group and user not waitin for acceptance"
        groupService.getGroup(group.id) >> group
        groupService.inviteUser(group, processed.id) >> { throw new InvalidAffiliationStateException() }

        when: "accepting other user"
        ResponseEntity responseEntity = groupApiDelegate.inviteUserToGroup(group.id, processed.id)

        then: "exception is thrown"
        thrown(InvalidAffiliationStateException.class)
    }


    void "member should fail to accept user to non-existing group"() {
        given: "non-existing group"
        groupService.getGroup(group.id) >> { throw new GroupNotFoundException() }

        when: "accepting other user"
        ResponseEntity responseEntity = groupApiDelegate.inviteUserToGroup(group.id, processed.id)

        then: "exception is thrown"
        thrown(GroupNotFoundException.class)
    }


    void "member should fail to accept non-existing user to his group"() {
        given: "member of group and non-existing user"
        groupService.getGroup(group.id) >> group
        groupService.inviteUser(group, processed.id) >> { throw new UserNotFoundException() }

        when: "accepting other user"
        ResponseEntity responseEntity = groupApiDelegate.inviteUserToGroup(group.id, processed.id)

        then: "exception is thrown"
        thrown(UserNotFoundException.class)
    }

    //reject user
    void "member should successfully reject user from his group"() {
        given: "authenticated user as member of group"

        when: "rejecting other user"
        ResponseEntity responseEntity = groupApiDelegate.rejectUserFromGroup(group.id, processed.id)

        then: "response is success"
        responseEntity.statusCode.'2xxSuccessful'
    }

    void "non-member should fail to reject user from his group"() {
        given: "not a member of group"
        groupService.getGroup(group.id) >> group
        groupService.rejectUserFromGroup(group, processed.id) >> { throw new NotGroupMemberException() }

        when: "rejecting other user"
        ResponseEntity responseEntity = groupApiDelegate.rejectUserFromGroup(group.id, processed.id)

        then: "exception is thrown"
        thrown(NotGroupMemberException.class)
    }

    void "member should fail to reject user not waiting for acceptance to his group"() {
        given: "member of group and user not waiting for acceptance"
        groupService.getGroup(group.id) >> group
        groupService.rejectUserFromGroup(group, processed.id) >> { throw new InvalidAffiliationStateException() }

        when: "rejecting other user"
        ResponseEntity responseEntity = groupApiDelegate.rejectUserFromGroup(group.id, processed.id)

        then: "exception is thrown"
        thrown(InvalidAffiliationStateException.class)
    }


    void "member should fail to reject user to non-existing group"() {
        given: "non-existing group"
        groupService.getGroup(group.id) >> { throw new GroupNotFoundException() }

        when: "rejecting other user"
        ResponseEntity responseEntity = groupApiDelegate.rejectUserFromGroup(group.id, processed.id)

        then: "exception is thrown"
        thrown(GroupNotFoundException.class)
    }


    void "member should fail to reject non-existing user to his group"() {
        given: "member of group and non-existing user"
        groupService.getGroup(group.id) >> group
        groupService.rejectUserFromGroup(group, processed.id) >> { throw new UserNotFoundException() }

        when: "rejecting other user"
        ResponseEntity responseEntity = groupApiDelegate.rejectUserFromGroup(group.id, processed.id)

        then: "exception is thrown"
        thrown(UserNotFoundException.class)
    }

    //join
    void "non-member should successfully request to join a group"() {
        given: "not a member"

        when: "joining group"
        ResponseEntity responseEntity = groupApiDelegate.joinGroup(group.id)

        then: "response is success"
        responseEntity.statusCode.is2xxSuccessful()
    }

    void "affiliated user should fail to request to join a group"() {
        given: "affiliated user"
        groupService.getGroup(group.id) >> group
        groupService.joinGroup(group) >> { throw new InvalidAffiliationStateException() }

        when: "joining group"
        ResponseEntity responseEntity = groupApiDelegate.joinGroup(group.id)

        then: "exception is thrown"
        thrown(InvalidAffiliationStateException.class)
    }

    void "user should fail to request to join a non-existing group"() {
        given: "non-existing group"
        groupService.getGroup(group.id) >> { throw new GroupNotFoundException() }

        when: "joining group"
        ResponseEntity responseEntity = groupApiDelegate.joinGroup(group.id)

        then: "exception is thrown"
        thrown(GroupNotFoundException.class)
    }

    //accept invitation
    void "invited user should successfully accept invitation to a group"() {
        given: "invited user"

        when: "accepting invitation"
        ResponseEntity responseEntity = groupApiDelegate.acceptInvitation(group.id)

        then: "response is success"
        responseEntity.statusCode.is2xxSuccessful()
    }

    void "not invited user should fail accept invitation to a group"() {
        given: "not invited user"
        groupService.getGroup(group.id) >> group
        groupService.acceptInvitation(group) >> { throw new InvalidAffiliationStateException() }

        when: "accepting invitation"
        ResponseEntity responseEntity = groupApiDelegate.acceptInvitation(group.id)

        then: "exception is thrown"
        thrown(InvalidAffiliationStateException.class)
    }

    void "invited user should fail to accept invitation to a non-existing group"() {
        given: "non-existing group"
        groupService.getGroup(group.id) >> { throw new GroupNotFoundException() }

        when: "accepting invitation"
        ResponseEntity responseEntity = groupApiDelegate.acceptInvitation(group.id)

        then: "exception is thrown"
        thrown(GroupNotFoundException.class)
    }

    //reject invitation
    void "invited user should successfully reject invitation to a group"() {
        given: "invited user"

        when: "rejecting invitation"
        ResponseEntity responseEntity = groupApiDelegate.rejectInvitation(group.id)

        then: "response is success"
        responseEntity.statusCode.is2xxSuccessful()
    }

    void "not invited user should fail reject invitation to a group"() {
        given: "not invited user"
        groupService.getGroup(group.id) >> group
        groupService.rejectInvitation(group) >> { throw new InvalidAffiliationStateException() }

        when: "rejecting invitation"
        ResponseEntity responseEntity = groupApiDelegate.rejectInvitation(group.id)

        then: "exception is thrown"
        thrown(InvalidAffiliationStateException.class)
    }

    void "invited user should fail to reject invitation to a non-existing group"() {
        given: "non-existing group"
        groupService.getGroup(group.id) >> { throw new GroupNotFoundException() }

        when: "rejecting invitation"
        ResponseEntity responseEntity = groupApiDelegate.rejectInvitation(group.id)

        then: "exception is thrown"
        thrown(GroupNotFoundException.class)
    }

    //leave
    void "affiliated user should successfully leave group"() {
        given: "affiliated user"

        when: "leaving group"
        ResponseEntity responseEntity = groupApiDelegate.leaveGroup(group.id)

        then: "response is success"
        responseEntity.statusCode.is2xxSuccessful()
    }

    void "non-affiliated user should fail to leave a group"() {
        given: "non-affiliated user"
        groupService.getGroup(group.id) >> group
        groupService.leaveGroup(group) >> { throw new InvalidAffiliationStateException() }

        when: "leaving group"
        ResponseEntity responseEntity = groupApiDelegate.leaveGroup(group.id)

        then: "exception is thrown"
        thrown(InvalidAffiliationStateException.class)
    }

    void "affiliated user should fail to leave a non-existing group"() {
        given: "non-existing group"
        groupService.getGroup(group.id) >> { throw new GroupNotFoundException() }

        when: "leaving group"
        ResponseEntity responseEntity = groupApiDelegate.leaveGroup(group.id)

        then: "exception is thrown"
        thrown(GroupNotFoundException.class)
    }

    //get tasks
    void "member should successfully get tasklist of a group"() {
        given: "group with tasklist"
        group.getTasks().add(task)
        groupService.getGroup(group.id) >> group
        groupService.getGroupTasks(group) >> group.tasks

        when: "user requests a tasklist"
        ResponseEntity<List<TaskDto>> responseEntity = groupApiDelegate.getTasks(group.id)

        then: "list is successfully returned"
        responseEntity.statusCode.'2xxSuccessful'
        responseEntity.body.asList().contains(TaskMapper.getInstance().toDto(task))
    }

    void "member should fail to get tasklist of a non-exisitng group"() {
        given: "non-existing group"
        groupService.getGroup(_) >> { throw new GroupNotFoundException() }

        when: "user requests a tasklist"
        ResponseEntity<List<TaskDto>> responseEntity = groupApiDelegate.getTasks(group.id)

        then: "exception is thrown"
        thrown(GroupNotFoundException.class)
    }

    //add tasks
    void "member should successfully add tasks to his group"() {
        given: "member and his group"
        userService.getAuthenticatedUser() >> authenticated
        groupService.getGroup(group.id) >> group
        groupService.createTask(*_) >> group

        when: "user adds new rankings to a group"
        ResponseEntity responseEntity = groupApiDelegate.createTask(group.id, taskInfoDto)

        then: "response is success"
        responseEntity.statusCode.'2xxSuccessful'
        responseEntity.headers.getLocation() == URI.create("/group/" + group.id + "/tasks")
    }

    void "member should fail to add task for a non-existing group"() {
        given: "non-existing group"
        userService.getAuthenticatedUser() >> authenticated
        groupService.getGroup(group.id) >> { throw new GroupNotFoundException() }

        when: "user adds ranking"
        ResponseEntity responseEntity = groupApiDelegate.createTask(group.id, taskInfoDto)

        then: "exception is thrown"
        thrown(GroupNotFoundException.class)
    }

    //get rankings
    void "member should successfully get rankings of a group"() {
        given: "group with rankings"
        group.getRankings().add(ranking)
        groupService.getGroup(group.id) >> group
        groupService.getGroupRankings(group) >> group.rankings

        when: "user requests a rankings"
        ResponseEntity<List<RankingDto>> responseEntity = groupApiDelegate.getRankings(group.id)

        then: "list is successfully returned"
        responseEntity.statusCode.'2xxSuccessful'
        responseEntity.body.asList().contains(RankingMapper.getInstance().toDto(ranking))
    }

    void "member should fail to get rankings of a non-exisitng group"() {
        given: "non-existing group"
        groupService.getGroup(_) >> { throw new GroupNotFoundException() }

        when: "user requests a rankings"
        ResponseEntity<List<RankingDto>> responseEntity = groupApiDelegate.getRankings(group.id)

        then: "exception is thrown"
        thrown(GroupNotFoundException.class)
    }

    //add rankings
    void "member should successfully add ranking to his group"() {
        given: "member and his group"
        userService.getAuthenticatedUser() >> authenticated
        groupService.getGroup(group.id) >> group
        groupService.createRanking(*_) >> group

        when: "user adds new task to a tasklist"
        ResponseEntity responseEntity = groupApiDelegate.createRanking(group.id, rankingDto)

        then: "response is success"
        responseEntity.statusCode.'2xxSuccessful'
        responseEntity.headers.getLocation() == URI.create("/group/" + group.id + "/rankings")
    }

    void "member should fail to add ranking for a non-existing group"() {
        given: "non-existing group"
        userService.getAuthenticatedUser() >> authenticated
        groupService.getGroup(group.id) >> { throw new GroupNotFoundException() }

        when: "user adds task"
        ResponseEntity responseEntity = groupApiDelegate.createRanking(group.id, rankingDto)

        then: "exception is thrown"
        thrown(GroupNotFoundException.class)
    }


    //add schedules
    void "member should successfully add schedule to his group"() {
        given: "member and his group"
        userService.getAuthenticatedUser() >> authenticated
        groupService.getGroup(group.id) >> group
        groupService.createSchedule(*_) >> group

        when: "user adds new task to a tasklist"
        ResponseEntity responseEntity = groupApiDelegate.createSchedule(group.id, scheduleDto)

        then: "response is success"
        responseEntity.statusCode.'2xxSuccessful'
        responseEntity.headers.getLocation() == URI.create("/group/" + group.id + "/schedules")
    }

    void "member should fail to add schedule for a non-existing group"() {
        given: "non-existing group"
        userService.getAuthenticatedUser() >> authenticated
        groupService.getGroup(group.id) >> { throw new GroupNotFoundException() }

        when: "user adds task"
        ResponseEntity responseEntity = groupApiDelegate.createSchedule(group.id, scheduleDto)

        then: "exception is thrown"
        thrown(GroupNotFoundException.class)
    }
}
