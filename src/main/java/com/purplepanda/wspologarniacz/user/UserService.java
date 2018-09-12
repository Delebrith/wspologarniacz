package com.purplepanda.wspologarniacz.user;

import org.springframework.data.domain.Page;

import java.util.Optional;

public interface UserService {
    Optional<User> getUser(Long id);
    Optional<User> authenticate(String email, String password);
    String getUsersToken(User user);
    User getAuthenticatedUser();
    User register(User user);
    void changePassword(User user, String password);
    void deregister(User user);
    void resetPassword(String token, String password);
    void requestPasswordReset(String email);
    void confirmRegistration(Long userId);
    Page<User> searchUsers(String name);
}
