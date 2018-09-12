package com.purplepanda.wspologarniacz.group;

import java.util.List;

public interface GroupService {
    List<Group> getAuthenticatedUsersGroups();
    void inviteUser(Long groupId, Long userId);
}
