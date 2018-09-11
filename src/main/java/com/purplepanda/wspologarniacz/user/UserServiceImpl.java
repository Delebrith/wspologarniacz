package com.purplepanda.wspologarniacz.user;

import com.purplepanda.wspologarniacz.user.event.PasswordResetRequestEvent;
import com.purplepanda.wspologarniacz.user.event.UserCreatedEvent;
import com.purplepanda.wspologarniacz.user.exception.IncorrectTokenException;
import com.purplepanda.wspologarniacz.user.exception.RequestNotFoundException;
import com.purplepanda.wspologarniacz.user.exception.UserAlreadyExistsException;
import com.purplepanda.wspologarniacz.user.exception.UserNotFoundException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.Date;
import java.util.UUID;

@Service
class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final RequestTokenRepository requestTokenRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final byte[] secretKey;
    private final ApplicationEventPublisher eventPublisher;
    private final String serverUrl;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           RequestTokenRepository requestTokenRepository,
                           String secretKey,
                           ApplicationEventPublisher eventPublisher,
                           @Value("${application.server-url}") String serverUrl) {
        this.userRepository = userRepository;
        this.requestTokenRepository = requestTokenRepository;
        this.eventPublisher = eventPublisher;
        this.serverUrl = serverUrl;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.secretKey = secretKey.getBytes();
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return userRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException(username));
    }

    @Override
    public Optional<User> getUser(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> authenticate(String email, String password) {
        final Optional<User> user = userRepository.findByEmail(email);
        final boolean passwordMatches =
                user.map(u -> passwordEncoder.matches(password, u.getPassword())).orElse(false);
        final boolean active = user.map(u ->  u.getActive() ).orElse(false);
        return (passwordMatches && active) ? user : Optional.empty();
    }

    @Override
    public String getUsersToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }

    @Override
    public User getAuthenticatedUser() {
        final String email = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Principal::getName)
                .orElseThrow(IllegalStateException::new);
        return userRepository.findByEmail(email).orElseThrow(IllegalStateException::new);
    }

    @Override
    public User register(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent())
            throw new UserAlreadyExistsException();

        user.setAuthorities(Collections.singletonList(AuthorityName.USER));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User created = userRepository.save(user);

        eventPublisher.publishEvent(UserCreatedEvent.builder()
                .user(created)
                .confirmationUrl(serverUrl + "/?userId=" + created.getId() + "/#confirm-registration")
                .build());

        return created;
    }

    @Override
    public void changePassword(User user, String password) {
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
    }

    @Override
    public void deregister(User user) {
        userRepository.delete(user);
    }

    @Override
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
        RequestToken resetRequestToken = RequestToken.builder()
                .token(UUID.randomUUID().toString())
                .requester(user)
                .expiresAt(LocalDateTime.now().plusMinutes(5L))
                .build();
        resetRequestToken = requestTokenRepository.save(resetRequestToken);
        eventPublisher.publishEvent(new PasswordResetRequestEvent(user,
                serverUrl + "/?token=" + resetRequestToken.getToken() + "/#password-reset"));
    }

    @Override
    public void confirmRegistration(Long userId) {
        User user = getUser(userId).orElseThrow(UserNotFoundException::new);
        user.setActive(true);
        userRepository.save(user);
    }

    @Override
    public void resetPassword(String token, String password) {
        RequestToken requestToken = requestTokenRepository.findByToken(token).orElseThrow(RequestNotFoundException::new);

        if (LocalDateTime.now().isAfter(requestToken.getExpiresAt())){
            throw new IncorrectTokenException();
        }

        User user  = requestToken.getRequester();
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        requestTokenRepository.delete(requestToken);
    }

}
