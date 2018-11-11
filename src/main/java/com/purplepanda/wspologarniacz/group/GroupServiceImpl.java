package com.purplepanda.wspologarniacz.group;

import com.purplepanda.wspologarniacz.group.authorization.GroupMemberAccess;
import com.purplepanda.wspologarniacz.group.exception.GroupNotFoundException;
import com.purplepanda.wspologarniacz.group.exception.InvalidAffiliationStateException;
import com.purplepanda.wspologarniacz.ranking.Ranking;
import com.purplepanda.wspologarniacz.task.Task;
import com.purplepanda.wspologarniacz.user.User;
import com.purplepanda.wspologarniacz.user.UserService;
import com.purplepanda.wspologarniacz.user.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GroupServiceImpl implements GroupService {

    private final UserService userService;
    private final GroupRepository groupRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public GroupServiceImpl(UserService userService,
                            GroupRepository groupRepository,
                            ApplicationEventPublisher eventPublisher) {
        this.userService = userService;
        this.groupRepository = groupRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public List<Group> getAuthenticatedUsersGroups() {
        User user = userService.getAuthenticatedUser();
        return groupRepository.findByAffiliationsUser(user);
    }

    @Transactional
    @Override
    public Page<Group> searchGroupsByName(String name, Pageable pageable) {
        return groupRepository.findByNameIgnoreCaseContaining(name, pageable);
    }

    @Transactional
    @Override
    @GroupMemberAccess
    public void inviteUser(Group group, Long userId) {
        User user = userService.getUser(userId).orElseThrow(UserNotFoundException::new);

        if (group.getAffiliations().stream()
                .anyMatch(a -> a.getUser().equals(user)) )
            throw new InvalidAffiliationStateException();

        Affiliation affiliation = Affiliation.builder()
                .user(user)
                .state(AffiliationState.PENDING_INVITATION)
                .lastUpdated(LocalDateTime.now())
                .build();

        group.getAffiliations().add(affiliation);
        groupRepository.save(group);
    }

    @Transactional
    @Override
    public void joinGroup(Group group) {
        User user = userService.getAuthenticatedUser();

        if (group.getAffiliations().stream()
                .anyMatch(a -> a.getUser().equals(user)) )
            throw new InvalidAffiliationStateException();

        Affiliation affiliation = Affiliation.builder()
                .user(user)
                .state(AffiliationState.WAITING_FOR_ACCEPTANCE)
                .lastUpdated(LocalDateTime.now())
                .build();

        group.getAffiliations().add(affiliation);
        groupRepository.save(group);
    }

    @Transactional
    @Override
    public void acceptInvitation(Group group) {
        User user = userService.getAuthenticatedUser();

        Affiliation affiliation = group.getAffiliations().stream()
                .filter(a -> a.getUser().equals(user) && a.getState().equals(AffiliationState.PENDING_INVITATION))
                .findFirst()
                .orElseThrow(InvalidAffiliationStateException::new);
        affiliation.setState(AffiliationState.MEMBER);
        updateResourceAccessRights(group);
        groupRepository.save(group);
    }

    @Transactional
    @Override
    @GroupMemberAccess
    public void acceptUserIntoGroup(Group group, Long userId) {
        User user = userService.getUser(userId).orElseThrow(UserNotFoundException::new);
        Affiliation affiliation = group.getAffiliations().stream()
                .filter(a -> a.getUser().equals(user) && a.getState().equals(AffiliationState.WAITING_FOR_ACCEPTANCE))
                .findFirst()
                .orElseThrow(InvalidAffiliationStateException::new);
        affiliation.setState(AffiliationState.MEMBER);
        groupRepository.save(group);
        updateResourceAccessRights(group);
    }

    @Transactional
    @Override
    public void rejectInvitation(Group group) {
        User user = userService.getAuthenticatedUser();

        Affiliation affiliation = group.getAffiliations().stream()
                .filter(a -> a.getUser().equals(user) && a.getState().equals(AffiliationState.PENDING_INVITATION))
                .findFirst()
                .orElseThrow(InvalidAffiliationStateException::new);

        group.getAffiliations().remove(affiliation);
        groupRepository.save(group);
    }

    @Transactional
    @Override
    @GroupMemberAccess
    public void rejectUserFromGroup(Group group, Long userId) {
        User user = userService.getUser(userId).orElseThrow(UserNotFoundException::new);

        Affiliation affiliation = group.getAffiliations().stream()
                .filter(a -> a.getUser().equals(user) && a.getState().equals(AffiliationState.WAITING_FOR_ACCEPTANCE))
                .findFirst()
                .orElseThrow(InvalidAffiliationStateException::new);

        group.getAffiliations().remove(affiliation);
        groupRepository.save(group);
    }

    @Transactional
    @Override
    @GroupMemberAccess
    public void leaveGroup(Group group) {
        User user = userService.getAuthenticatedUser();

        Affiliation affiliation = group.getAffiliations().stream()
                .filter(a -> a.getUser().equals(user)
                        && (a.getState().equals(AffiliationState.MEMBER) || a.getState().equals(AffiliationState.WAITING_FOR_ACCEPTANCE)))
                .findFirst()
                .orElseThrow(InvalidAffiliationStateException::new);

        group.getAffiliations().remove(affiliation);
        if (group.getAffiliations().isEmpty()) {
            groupRepository.delete(group);
            updateResourceAccessRights(group);
            return;
        }
        groupRepository.save(group);
        updateResourceAccessRights(group);
    }

    @Override
    public Group createGroup(Group group) {
        group = groupRepository.save(group);
        Affiliation affiliation = Affiliation.builder()
                .lastUpdated(LocalDateTime.now())
                .state(AffiliationState.MEMBER)
                .user(userService.getAuthenticatedUser())
                .build();
        group.getAffiliations().add(affiliation);
        return groupRepository.save(group);
    }

    @Override
    public Group getGroup(Long groupId) {
        return groupRepository.findById(groupId).orElseThrow(GroupNotFoundException::new);
    }

    @Override
    @GroupMemberAccess
    public Group createTask(Group group, Task task) {
        task.setAuthorized(getGroupMembers(group));
        group.getTasks().add(task);
        return groupRepository.save(group);
    }

    @Override
    @GroupMemberAccess
    public Group createRanking(Group group, Ranking ranking) {
        validateRankingParticipants(group, ranking);
        group.getRankings().add(ranking);
        groupRepository.save(group);
        return null;
    }

    private Set<User> getGroupMembers(Group group) {
        return group.getAffiliations().stream()
                .filter(a -> a.getState().equals(AffiliationState.MEMBER))
                .map(a -> a.getUser())
                .collect(Collectors.toSet());
    }

    private void validateRankingParticipants(Group group, Ranking ranking) {
        Set<User> possibleParticipants  = getGroupMembers(group);
        if (!ranking.getCategories().stream()
                .flatMap(c -> c.getScores().stream())
                .map(s -> s.getUser())
                .allMatch(u -> possibleParticipants.contains(u))) {
            throw new IllegalArgumentException("Participants of the ranking must be group members");
        }
    }

    private void updateResourceAccessRights(Group group) {
        Set<User> members = getGroupMembers(group);
        eventPublisher.publishEvent(new GroupMemberListUpdatedEvent(members, group));
    }
}
