package com.purplepanda.wspologarniacz.api;

import com.purplepanda.wspologarniacz.api.model.*;
import com.purplepanda.wspologarniacz.user.User;
import com.purplepanda.wspologarniacz.user.UserMapper;
import com.purplepanda.wspologarniacz.user.UserService;
import com.purplepanda.wspologarniacz.user.exception.UserNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

@Component
public class UserApiDelegateImpl implements UserApiDelegate {

    private final UserService userService;
    private final UserMapper userMapper;

    public UserApiDelegateImpl(UserService userService) {
        this.userService = userService;
        userMapper = UserMapper.getInstance();
    }

    @Override
    public ResponseEntity<AuthorizationTokenDto> login(UserCredentialsDto credentials) {
        if (credentials.getPassword() == null || credentials.getPassword().isEmpty())
            throw new UserNotFoundException();
        User authorized =  userService.authenticate(credentials.getEmail(), credentials.getPassword())
                .orElseThrow(UserNotFoundException::new);
        return ResponseEntity.ok(new AuthorizationTokenDto().token(userService.getUsersToken(authorized)));
    }

    @Override
    public ResponseEntity<Void> register(UserDto userData)  {
        User created = userService.register(userMapper.fromDto(userData));
        try {
            return ResponseEntity.created(new URI("/user/find/" + created.getId())).build();
        } catch (URISyntaxException e) {
            log.error(e.getMessage());
            return ResponseEntity.status(500).build(); // should never happen
        }
    }

    @Override
    public ResponseEntity<Void> changePassword(PasswordDto password) {
        User modified = userService.getAuthenticatedUser();
        userService.changePassword(modified, password.getPassword());
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> deregister() {
        User deregistered = userService.getAuthenticatedUser();
        userService.deregister(deregistered);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<UserDto> getMyDetails() {
        return ResponseEntity.ok(userMapper.toDto(userService.getAuthenticatedUser()));
    }

    @Override
    public ResponseEntity<Void> requestPasswordReset(UserCredentialsDto credentials) {
        userService.requestPasswordReset(credentials.getEmail());
        return ResponseEntity.accepted().build();
    }

    @Override
    public ResponseEntity<Void> resetPassword(PasswordDto password, UUID token) {
        userService.resetPassword(token.toString(), password.getPassword());
        return ResponseEntity.accepted().build();
    }

    @Override
    public ResponseEntity<Void> confirm(Long userId) {
        userService.confirmRegistration(userId);
        return ResponseEntity.accepted().build();
    }
}
