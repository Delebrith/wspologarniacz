package com.purplepanda.wspologarniacz.api

import com.purplepanda.wspologarniacz.api.model.GroupDto
import com.purplepanda.wspologarniacz.api.model.TaskInfoDto
import com.purplepanda.wspologarniacz.group.Affiliation
import com.purplepanda.wspologarniacz.group.AffiliationState
import com.purplepanda.wspologarniacz.group.Group
import com.purplepanda.wspologarniacz.group.GroupMapper
import com.purplepanda.wspologarniacz.group.GroupService
import com.purplepanda.wspologarniacz.group.exception.GroupNotFoundException
import com.purplepanda.wspologarniacz.task.Task
import com.purplepanda.wspologarniacz.user.AuthorityName
import com.purplepanda.wspologarniacz.user.User
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import spock.lang.Specification
import java.time.LocalDateTime

class GroupApiDelegateSpec extends Specification {

    //mocked
    private GroupService groupService

    //tested
    private GroupApiDelegate groupApiDelegate

    //test data
    private User authenticated
    private User processed
    private Group group
    private Task task
    private TaskInfoDto taskInfoDto

    void setup() {
        groupService = Mock(GroupService.class)
        groupApiDelegate = new GroupApiDelegateImpl(groupService)

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
                .build()

        taskInfoDto = new TaskInfoDto()
                .name("task")
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

        when: "user requests hid groups info"
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
}
