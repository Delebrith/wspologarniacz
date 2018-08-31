package com.purplepanda.wspologarniacz.user;

import com.purplepanda.wspologarniacz.user.event.UserCreatedEvent;
import com.purplepanda.wspologarniacz.user.exception.UserAlreadyExistsException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import net.bytebuddy.utility.RandomString;
import org.hibernate.cfg.NotYetImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Collections;
import java.util.Optional;
import java.util.Date;

@Service
class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final byte[] secretKey;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           String secretKey,
                           ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
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
    public void resetPassword(String email) {
        throw new NotYetImplementedException();
    }

    private String generatePassword() {
        return RandomString.make(8);
    }
}
