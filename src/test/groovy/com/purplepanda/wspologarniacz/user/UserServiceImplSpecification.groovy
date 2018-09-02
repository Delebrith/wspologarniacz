package com.purplepanda.wspologarniacz.user

import com.purplepanda.wspologarniacz.user.exception.UserAlreadyExistsException
import io.jsonwebtoken.Jwts
import org.springframework.context.ApplicationEventPublisher
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import spock.lang.Specification

import java.nio.charset.Charset;

class UserServiceImplSpecification extends Specification {
    // tested
    private UserServiceImpl userService

    // mocked
    private UserRepository userRepository
    private RequestRepository requestRepository;
    private BCryptPasswordEncoder passwordEncoder
    private ApplicationEventPublisher eventPublisher

    // data
    private String secret
    private String email
    private String password
    private User user

    void setup() {
        secret = "5up3r53cr3t"
        email = "some@email.com"
        password = "some-password"
        user = User.builder()
            .id(0)
            .email(email)
            .password("\$2a\$04\$PVp1C6aQcl3ris4303XRbuJHng4yE8Y8Uke.2i5Nez1g/2n0KfMuG")
            .build()
        userRepository = Mock(UserRepository.class)
        passwordEncoder = new BCryptPasswordEncoder()
        eventPublisher = Mock(ApplicationEventPublisher.class)
        userService = new UserServiceImpl(userRepository, requestRepository, secret, eventPublisher)
    }

    void "user should be authenticated with existing email and valid password"() {
        given: "valid password"
        user = User.builder()
                .email(email)
                .password("\$2a\$04\$PVp1C6aQcl3ris4303XRbuJHng4yE8Y8Uke.2i5Nez1g/2n0KfMuG")
                .build()

        and: "existing email"
        userRepository.findByEmail(email) >> Optional.ofNullable(user)

        when: "USER tries authentication"
        Optional<User> result = userService.authenticate(email, password)

        then: "authentication succeeds"
        result == Optional.ofNullable(user)
    }

    void "user should not be authenticated with existing email and invalid password"() {
        given: "invalid password"
        user = User.builder()
                .email(email)
                .password("invalid")
                .build()

        and: "existing email"
        userRepository.findByEmail(email) >> Optional.ofNullable(user)

        when: "USER tries authentication"
        Optional<User> result = userService.authenticate(email, password)

        then: "authentication fails"
        result == Optional.empty()
    }

    void "user should not be authenticated with non-existing email"() {
        given: "non-existing email"
        userRepository.findByEmail(email) >> Optional.empty()

        when: "USER tries authentication"
        Optional<User> result = userService.authenticate(email, password)

        then: "authentication fails"
        result == Optional.empty()
    }

    void "user should get correct token"() {
        given: "USER's email"
        when: "token generation is invoked"
        String result = userService.getUsersToken(user)

        then: "token has USER as subject"
        user.email == Jwts.parser()
                .setSigningKey(secret.getBytes(Charset.forName("UTF-8")))
                .parseClaimsJws(result)
                .getBody()
                .getSubject()
    }

    void "existing user should be loaded"() {
        given: "existing USER's email"
            userRepository.findByEmail(email) >> Optional.ofNullable(user)

        when: "loading USER is invoked"
            UserDetails result = userService.loadUserByUsername(email)

        then: "USER is loaded"
            result == user
    }

    void "non-existing user should not be loaded"() {
        given: "existing USER's email"
        userRepository.findByEmail(email) >> Optional.empty()

        when: "loading USER is invoked"
        UserDetails result = userService.loadUserByUsername(email)

        then: "exception is thrown"
        thrown(UsernameNotFoundException)
    }

    void "existing user info should be found"() {
        given: "existing USER's email"
        userRepository.findById(user.id) >> Optional.ofNullable(user)

        when: "getting info USER is invoked"
        Optional<User> result = userService.getUserInfo(user.id)

        then: "USER info is loaded"
        result == Optional.ofNullable(user)
    }

    void "non-existing user info should not be loaded"() {
        given: "existing USER's email"
        userRepository.findById(user.id) >> Optional.empty()

        when: "loading USER is invoked"
        Optional<User> result = userService.getUserInfo(user.id)

        then: "USER info is not loaded"
        result == Optional.empty()
    }


    void "non-existing user should be registered"() {
        given: "non-existing USER's email"
        userRepository.findByEmail(email) >> Optional.empty()
        userRepository.save(user) >> user

        when: "registering USER is invoked"
        User result = userService.register(user)

        then: "USER is registered"
        result == user
    }

    void "existing user should not be registered"() {
        given: "existing USER's email"
        userRepository.findByEmail(email) >> Optional.ofNullable(user)

        when: "getting info USER is invoked"
        User result = userService.register(user)

        then: "Exception is thrown"
        thrown(UserAlreadyExistsException)
    }

    void "password of valid user should be changed"() {
        given: "existing USER"

        when: "password change is invoked"
        userService.changePassword(user, "new-password")

        then: "USER with new password is saved"
        1 * userRepository.save(user)
    }

    void "existing user should be deregistered"() {
        given: "existing USER's email"
        userRepository.findById(user.id) >> Optional.ofNullable(user)

        when: "deregistration is invoked"
        userService.deregister(user)

        then: "USER is deleted"
        1 * userRepository.delete(user)
    }

}