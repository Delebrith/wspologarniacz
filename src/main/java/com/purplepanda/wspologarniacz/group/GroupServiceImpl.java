package com.purplepanda.wspologarniacz.group;

import com.purplepanda.wspologarniacz.group.exception.GroupNotFoundException;
import com.purplepanda.wspologarniacz.group.exception.InvalidAffiliationStateException;
import com.purplepanda.wspologarniacz.group.exception.NotGroupMemberException;
import com.purplepanda.wspologarniacz.task.Task;
import com.purplepanda.wspologarniacz.task.TaskStatus;
import com.purplepanda.wspologarniacz.user.User;
import com.purplepanda.wspologarniacz.user.UserService;
import com.purplepanda.wspologarniacz.user.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public GroupServiceImpl(UserService userService,
                            GroupRepository groupRepository) {
        this.userService = userService;
        this.groupRepository = groupRepository;
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
    public void inviteUser(Long groupId, Long userId) {
        User user = userService.getUser(userId).orElseThrow(UserNotFoundException::new);
        Group group = groupRepository.findById(groupId).orElseThrow(GroupNotFoundException::new);

        checkAccessRights(group);

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
    public void joinGroup(Long groupId) {
        User user = userService.getAuthenticatedUser();
        Group group = groupRepository.findById(groupId).orElseThrow(GroupNotFoundException::new);

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
    public void acceptInvitation(Long groupId) {
        User user = userService.getAuthenticatedUser();
        Group group = groupRepository.findById(groupId).orElseThrow(GroupNotFoundException::new);

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
    public void acceptUserIntoGroup(Long groupId, Long userId) {
        User user = userService.getUser(userId).orElseThrow(UserNotFoundException::new);
        Group group = groupRepository.findById(groupId).orElseThrow(GroupNotFoundException::new);

        checkAccessRights(group);

        Affiliation affiliation = group.getAffiliations().stream()
                .filter(a -> a.getUser().equals(user) && a.getState().equals(AffiliationState.WAITING_FOR_ACCEPTANCE))
                .findFirst()
                .orElseThrow(InvalidAffiliationStateException::new);
        affiliation.setState(AffiliationState.MEMBER);
        updateResourceAccessRights(group);

        groupRepository.save(group);
    }

    @Transactional
    @Override
    public void rejectInvitation(Long groupId) {
        User user = userService.getAuthenticatedUser();
        Group group = groupRepository.findById(groupId).orElseThrow(GroupNotFoundException::new);

        Affiliation affiliation = group.getAffiliations().stream()
                .filter(a -> a.getUser().equals(user) && a.getState().equals(AffiliationState.PENDING_INVITATION))
                .findFirst()
                .orElseThrow(InvalidAffiliationStateException::new);

        group.getAffiliations().remove(affiliation);
        groupRepository.save(group);
    }

    @Transactional
    @Override
    public void rejectUserFromGroup(Long groupId, Long userId) {
        User user = userService.getUser(userId).orElseThrow(UserNotFoundException::new);
        Group group = groupRepository.findById(groupId).orElseThrow(GroupNotFoundException::new);

        checkAccessRights(group);

        Affiliation affiliation = group.getAffiliations().stream()
                .filter(a -> a.getUser().equals(user) && a.getState().equals(AffiliationState.WAITING_FOR_ACCEPTANCE))
                .findFirst()
                .orElseThrow(InvalidAffiliationStateException::new);

        group.getAffiliations().remove(affiliation);
        groupRepository.save(group);
    }

    @Transactional
    @Override
    public void leaveGroup(Long groupId) {
        User user = userService.getAuthenticatedUser();
        Group group = groupRepository.findById(groupId).orElseThrow(GroupNotFoundException::new);

        Affiliation affiliation = group.getAffiliations().stream()
                .filter(a -> a.getUser().equals(user) && a.getState().equals(AffiliationState.MEMBER))
                .findFirst()
                .orElseThrow(InvalidAffiliationStateException::new);

        group.getAffiliations().remove(affiliation);
        updateResourceAccessRights(group);

        if (group.getAffiliations().isEmpty()) {
            groupRepository.delete(group);
            return;
        }

        groupRepository.save(group);
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
    public Group createTask(Long groupId, String name, String description) {
        Group group = getGroup(groupId);
        checkAccessRights(group);

        Set<User> authorized  = group.getAffiliations().stream()
                .filter(a -> a.getState().equals(AffiliationState.MEMBER))
                .map(Affiliation::getUser)
                .collect(Collectors.toSet());

        Task created = Task.builder()
                .name(name)
                .description(description)
                .lastModifiedBy(userService.getAuthenticatedUser())
                .status(TaskStatus.ADDED)
                .updateTime(LocalDateTime.now())
                .authorized(authorized)
                .build();

        group.getTasks().add(created);
        return groupRepository.save(group);
    }

    private void checkAccessRights(Group group) {
        User authenticated = userService.getAuthenticatedUser();
        group.getAffiliations().stream()
                .filter(a -> a.getUser().equals(authenticated) && a.getState().equals(AffiliationState.MEMBER))
                .findFirst()
                .orElseThrow(NotGroupMemberException::new);
    }

    private void updateResourceAccessRights(Group group) {
        Set<User> authorized = group.getAffiliations().stream()
                .filter(a -> a.getState().equals(AffiliationState.MEMBER))
                .map(a -> a.getUser())
                .collect(Collectors.toSet());
        group.getTasks()
                .forEach(t -> t.setAuthorized(authorized));
        // to be expanded
    }
}
