package com.purplepanda.wspologarniacz.group;

import com.purplepanda.wspologarniacz.api.GroupApiDelegate;
import com.purplepanda.wspologarniacz.api.model.GroupDto;
import com.purplepanda.wspologarniacz.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

public class GroupApiDelegateImpl implements GroupApiDelegate {

    private final GroupService groupService;
    private final UserService userService;

    @Autowired
    public GroupApiDelegateImpl(GroupService groupService,
                                UserService userService) {
        this.groupService = groupService;
        this.userService = userService;
    }

    @Override
    public ResponseEntity<GroupDto> getMyGroups() {
        return null;
    }
}
