package com.purplepanda.wspologarniacz.api;

import com.purplepanda.wspologarniacz.api.model.GroupDto;
import com.purplepanda.wspologarniacz.api.model.RankingDto;
import com.purplepanda.wspologarniacz.api.model.TaskDto;
import com.purplepanda.wspologarniacz.api.model.TaskInfoDto;
import com.purplepanda.wspologarniacz.group.Group;
import com.purplepanda.wspologarniacz.group.GroupMapper;
import com.purplepanda.wspologarniacz.group.GroupService;
import com.purplepanda.wspologarniacz.ranking.RankingMapper;
import com.purplepanda.wspologarniacz.task.TaskMapper;
import com.purplepanda.wspologarniacz.user.UserService;
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
    private final UserService userService;
    private final GroupMapper groupMapper = GroupMapper.getInstance();
    private final TaskMapper taskMapper = TaskMapper.getInstance();
    private final RankingMapper rankingMapper = RankingMapper.getInstance();

    @Autowired
    public GroupApiDelegateImpl(GroupService groupService, UserService userService) {
        this.groupService = groupService;
        this.userService = userService;
    }

    @Override
    public ResponseEntity<Void> acceptInvitation(Long groupId) {
        Group group = groupService.getGroup(groupId);
        groupService.acceptInvitation(group);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> acceptUserIntoGroup(Long groupId, Long userId) {
        Group group = groupService.getGroup(groupId);
        groupService.acceptUserIntoGroup(group, userId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> rejectInvitation(Long groupId) {
        Group group = groupService.getGroup(groupId);
        groupService.rejectInvitation(group);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> rejectUserFromGroup(Long groupId, Long userId) {
        Group group = groupService.getGroup(groupId);
        groupService.rejectUserFromGroup(group, userId);
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
        Group group = groupService.getGroup(groupId);
        groupService.inviteUser(group, userId);
        return ResponseEntity.accepted().build();
    }

    @Override
    public ResponseEntity<Void> joinGroup(Long groupId) {
        Group group = groupService.getGroup(groupId);
        groupService.joinGroup(group);
        return ResponseEntity.accepted().build();
    }

    @Override
    public ResponseEntity<Void> leaveGroup(Long groupId) {
        Group group = groupService.getGroup(groupId);
        groupService.leaveGroup(group);
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
        Group group = groupService.getGroup(groupId);
        Group modified = groupService.createTask(
                group, taskMapper.toEntity(taskInfoDto, userService.getAuthenticatedUser()));
        return ResponseEntity.created(
                URI.create("/group/" + modified.getId() + "/tasks"))
                .build();
    }

    @Override
    public ResponseEntity<List<TaskDto>> getTasks(Long groupId) {
        Group group = groupService.getGroup(groupId);
        return ResponseEntity.ok(groupService.getGroupTasks(group).stream()
            .map(taskMapper::toDto)
            .collect(Collectors.toList())
        );
    }

    @Override
    public ResponseEntity<Void> createRanking(Long groupId, RankingDto rankingDto) {
        Group group = groupService.getGroup(groupId);
        Group modified = groupService.createRanking(group, rankingMapper.fromDto(rankingDto));
        return ResponseEntity.created(
                URI.create("/group/" + modified.getId() + "/rankings"))
                .build();
    }

    @Override
    public ResponseEntity<List<RankingDto>> getRankings(Long groupId) {
        Group group = groupService.getGroup(groupId);
        return ResponseEntity.ok(groupService.getGroupRankings(group).stream()
                .map(rankingMapper::toDto)
                .collect(Collectors.toList())
        );
    }
}
