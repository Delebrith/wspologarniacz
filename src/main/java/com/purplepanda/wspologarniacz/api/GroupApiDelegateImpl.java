package com.purplepanda.wspologarniacz.api;

import com.purplepanda.wspologarniacz.api.model.GroupDto;
import com.purplepanda.wspologarniacz.api.model.TaskDto;
import com.purplepanda.wspologarniacz.api.model.TaskInfoDto;
import com.purplepanda.wspologarniacz.group.Group;
import com.purplepanda.wspologarniacz.group.GroupMapper;
import com.purplepanda.wspologarniacz.group.GroupService;
import com.purplepanda.wspologarniacz.task.TaskMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;


@Component
public class GroupApiDelegateImpl implements GroupApiDelegate {

    private final GroupService groupService;
    private final GroupMapper groupMapper = GroupMapper.getInstance();
    private final TaskMapper taskMapper = TaskMapper.getInstance();

    @Autowired
    public GroupApiDelegateImpl(GroupService groupService) {
        this.groupService = groupService;
    }

    @Override
    public ResponseEntity<Void> acceptInvitation(Long groupId) {
        groupService.acceptInvitation(groupId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> acceptUserIntoGroup(Long groupId, Long userId) {
        groupService.acceptUserIntoGroup(groupId, userId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> rejectInvitation(Long groupId) {
        groupService.rejectInvitation(groupId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> rejectUserFromGroup(Long groupId, Long userId) {
        groupService.rejectUserFromGroup(groupId, userId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<GroupDto>> getMyGroups() {
        List<GroupDto> responseBody = groupService.getAuthenticatedUsersGroups()
                .stream()
                .map(groupMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseBody);
    }

    @Override
    public ResponseEntity<Void> inviteUserToGroup(Long groupId, Long userId) {
        groupService.inviteUser(groupId, userId);
        return ResponseEntity.accepted().build();
    }

    @Override
    public ResponseEntity<Void> joinGroup(Long groupId) {
        groupService.joinGroup(groupId);
        return ResponseEntity.accepted().build();
    }

    @Override
    public ResponseEntity<Void> leaveGroup(Long groupId) {
        groupService.leaveGroup(groupId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<GroupDto>> searchGroups(String name, Integer size, Integer number) {
        List<GroupDto> responseBody = groupService.searchGroupsByName(name, PageRequest.of(number, size))
                .map(groupMapper::toDto)
                .getContent();
        return ResponseEntity.ok(responseBody);
    }

    @Override
    public ResponseEntity<Void> createGroup(GroupDto groupDto) {
        Group created = groupService.createGroup(groupMapper.fromDto(groupDto));
        return ResponseEntity.created(URI.create("/group/find/" + created.getId())).build();
    }

    @Override
    public ResponseEntity<GroupDto> getGroup(Long groupId) {
        return ResponseEntity.ok(groupMapper.toDto(groupService.getGroup(groupId)));
    }

    @Override
    public ResponseEntity<Void> createTask(Long groupId, TaskInfoDto taskInfoDto) {
        Group modified = groupService.createTask(groupId, taskInfoDto.getName(), taskInfoDto.getDescription());
        return ResponseEntity.created(
                URI.create("/group/" + modified.getId() + "/tasks"))
                .build();
    }

    @Override
    public ResponseEntity<List<TaskDto>> getTasks(Long groupId) {
        return ResponseEntity.ok(groupService.getGroup(groupId).getTasks().stream()
            .map(taskMapper::toDto)
            .collect(Collectors.toList())
        );
    }
}
