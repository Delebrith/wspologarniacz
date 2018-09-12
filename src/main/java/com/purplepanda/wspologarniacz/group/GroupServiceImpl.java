package com.purplepanda.wspologarniacz.group;

import com.purplepanda.wspologarniacz.group.exception.GroupNotFoundException;
import com.purplepanda.wspologarniacz.group.exception.InvalidAffiliationStateException;
import com.purplepanda.wspologarniacz.user.User;
import com.purplepanda.wspologarniacz.user.UserService;
import com.purplepanda.wspologarniacz.user.exception.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GroupServiceImpl implements GroupService {

    private final UserService userService;
    private final GroupRepository groupRepository;

    public GroupServiceImpl(UserService userService,
                            GroupRepository groupRepository) {
        this.userService = userService;
        this.groupRepository = groupRepository;
    }

    @Transactional
    @Override
    public List<Group> getAuthenticatedUsersGroups() {
        User user = userService.getAuthenticatedUser();
        Set<Affiliation> affiliations = user.getAffiliations();
        if (affiliations.isEmpty())
            return Collections.emptyList();
        return affiliations.stream()
                .map(Affiliation::getGroup)
                .collect(Collectors.toList());
    }

    @Override
    public void inviteUser(Long groupId, Long userId) {
        User user = userService.getUser(userId).orElseThrow(UserNotFoundException::new);
        Group group = groupRepository.findById(groupId).orElseThrow(GroupNotFoundException::new);

        if (group.getAffiliations().stream()
                .anyMatch(a -> a.getUser().equals(user)) )
            throw new InvalidAffiliationStateException();

        Affiliation affiliation = Affiliation.builder()
                .group(group)
                .user(user)
                .state(AffiliationState.PENDING_INVITATION)
                .lastUpdated(LocalDateTime.now())
                .build();

        group.getAffiliations().add(affiliation);
        groupRepository.save(group);
    }
}
