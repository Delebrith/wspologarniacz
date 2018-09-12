package com.purplepanda.wspologarniacz.api;

import com.purplepanda.wspologarniacz.api.model.GroupDto;
import com.purplepanda.wspologarniacz.group.GroupMapper;
import com.purplepanda.wspologarniacz.group.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;


@Component
public class GroupApiDelegateImpl implements GroupApiDelegate {

    private final GroupService groupService;
    private final GroupMapper groupMapper = GroupMapper.getInstance();

    @Autowired
    public GroupApiDelegateImpl(GroupService groupService) {
        this.groupService = groupService;
    }

    @Override
    public ResponseEntity<Void> acceptInvitation(Long groupId) {
        return null;
    }

    @Override
    public ResponseEntity<Void> acceptUserIntoGroup(Long groupId, Long userId) {
        return null;
    }

    @Override
    public ResponseEntity<Void> rejectInvitation(Long groupId) {
        return null;
    }

    @Override
    public ResponseEntity<Void> rejectUserIntoGroup(Long groupId, Long userId) {
        return null;
    }

    @Override
    public ResponseEntity<List<GroupDto>> getMyGroups() {
        return ResponseEntity
                .ok(groupService.getAuthenticatedUsersGroups().stream()
                    .map(groupMapper::toDto)
                    .collect(Collectors.toList()));
    }

    @Override
    public ResponseEntity<Void> inviteUserToGroup(Long groupId, Long userId) {
        groupService.inviteUser(groupId, userId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> joinGroup(Long groupId) {
        return null;
    }

    @Override
    public ResponseEntity<Void> leaveGroup(Long groupId) {
        return null;
    }

    @Override
    public ResponseEntity<List<GroupDto>> searchGroups(String name, Integer size, Integer number) {
        return null;
    }
}
