package com.purplepanda.wspologarniacz.group;

import com.purplepanda.wspologarniacz.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@AllArgsConstructor
@Getter
public class GroupMemberListUpdatedEvent {
    private Set<User> members;
    private Group group;
}
