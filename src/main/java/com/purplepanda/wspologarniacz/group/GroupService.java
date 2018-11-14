package com.purplepanda.wspologarniacz.group;

import com.purplepanda.wspologarniacz.ranking.Ranking;
import com.purplepanda.wspologarniacz.task.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

public interface GroupService {
    List<Group> getAuthenticatedUsersGroups();

    Page<Group> searchGroupsByName(String name, Pageable pageable);

    void inviteUser(Group group, Long userId);

    void joinGroup(Group group);

    void acceptInvitation(Group group);

    void rejectInvitation(Group group);

    void acceptUserIntoGroup(Group group, Long userId);

    void rejectUserFromGroup(Group group, Long userId);

    void leaveGroup(Group group);

    Group createGroup(Group group);

    Group getGroup(Long groupId);

    Group createTask(Group group, Task task);

    Set<Task> getGroupTasks(Group group);

    Group createRanking(Group group, Ranking ranking);

    Set<Ranking> getGroupRankings(Group group);
}
