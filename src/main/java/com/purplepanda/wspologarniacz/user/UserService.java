package com.purplepanda.wspologarniacz.user;

import java.util.Optional;

public interface UserService {
    Optional<User> getUserInfo(Long id);
    Optional<User> authenticate(String email, String password);
    String getUsersToken(User user);
    User getAuthenticatedUser();
    User register(User user);
    void changePassword(User user, String password);
    void deregister(User user);
    void resetPassword(String email);
}
