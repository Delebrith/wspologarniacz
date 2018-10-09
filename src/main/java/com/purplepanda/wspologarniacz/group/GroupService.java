package com.purplepanda.wspologarniacz.group;

import com.purplepanda.wspologarniacz.task.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface GroupService {
    List<Group> getAuthenticatedUsersGroups();
    Page<Group> searchGroupsByName(String name, Pageable pageable);
    void inviteUser(Long groupId, Long userId);
    void joinGroup(Long groupId);
    void acceptInvitation(Long groupId);
    void rejectInvitation(Long groupId);
    void acceptUserIntoGroup(Long groupId, Long userId);
    void rejectUserFromGroup(Long groupId, Long userId);
    void leaveGroup(Long groupId);
    Group createGroup(Group group);
    Group getGroup(Long groupId);

    Group createTask(Long groupId, String name, String description);
}
