package com.purplepanda.wspologarniacz.user;

import com.purplepanda.wspologarniacz.user.event.PasswordResetEvent;
import com.purplepanda.wspologarniacz.user.event.PasswordResetRequestEvent;
import com.purplepanda.wspologarniacz.user.event.UserCreatedEvent;
import com.purplepanda.wspologarniacz.user.exception.IncorrectRequestState;
import com.purplepanda.wspologarniacz.user.exception.RequestNotFoundException;
import com.purplepanda.wspologarniacz.user.exception.UserAlreadyExistsException;
import com.purplepanda.wspologarniacz.user.exception.UserNotFoundException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import net.bytebuddy.utility.RandomString;
import org.springframework.beans.factory.annotation.Autowired;
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

@Service
class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final byte[] secretKey;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           RequestRepository requestRepository,
                           String secretKey,
                           ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.requestRepository = requestRepository;
        this.eventPublisher = eventPublisher;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.secretKey = secretKey.getBytes();
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return userRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException(username));
    }

    @Override
    public Optional<User> getUserInfo(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> authenticate(String email, String password) {
        final Optional<User> user = userRepository.findByEmail(email);
        final boolean passwordMatches =
                user.map(u -> passwordEncoder.matches(password, u.getPassword())).orElse(false);
        return passwordMatches ? user : Optional.empty();
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
        user.setPassword(generatePassword());
        eventPublisher.publishEvent(UserCreatedEvent.builder()
                .user(user)
                .build());

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
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
        Token resetToken = Token.builder()
                .requester(user)
                .limit(LocalDateTime.now().plusMinutes(2))
                .build();
        resetToken = requestRepository.save(resetToken);
        eventPublisher.publishEvent(new PasswordResetRequestEvent(user,
                "/user/password/reset/confirm/" + user.getId() + "/" + resetToken.getId()));
    }

    @Override
    public void resetPassword(Long requestId, Long userId) {
        Token token = requestRepository.findById(requestId).orElseThrow(RequestNotFoundException::new);
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        if (!token.getRequester().equals(user) || LocalDateTime.now().isAfter(token.getLimit())){
            throw new IncorrectRequestState();
        }
        user.setPassword(generatePassword());
        eventPublisher.publishEvent(PasswordResetEvent.builder()
                .user(user)
                .build());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        requestRepository.delete(token);
    }

    private String generatePassword() {
        return RandomString.make(8);
    }
}
