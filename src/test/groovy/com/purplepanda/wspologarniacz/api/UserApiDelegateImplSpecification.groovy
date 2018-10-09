package com.purplepanda.wspologarniacz.api

import com.purplepanda.wspologarniacz.api.model.AuthorizationTokenDto
import com.purplepanda.wspologarniacz.api.model.PasswordDto
import com.purplepanda.wspologarniacz.api.model.UserCredentialsDto
import com.purplepanda.wspologarniacz.api.model.UserDto
import com.purplepanda.wspologarniacz.user.User
import com.purplepanda.wspologarniacz.user.UserMapper
import com.purplepanda.wspologarniacz.user.UserService
import com.purplepanda.wspologarniacz.user.exception.UserAlreadyExistsException
import com.purplepanda.wspologarniacz.user.exception.UserNotFoundException
import org.springframework.http.ResponseEntity
import spock.lang.Specification

class UserApiDelegateImplSpecification extends Specification {

    //Mocked
    private UserService userService

    //test data
    private UserCredentialsDto credentialsDto
    private UserDto userDto
    private User user
    private PasswordDto passwordDto

    //to be tested
    private UserApiDelegateImpl userApiDelegate;

    void setup() {
        userService = Mock(UserService.class)
        userApiDelegate = new UserApiDelegateImpl(userService)
        credentialsDto = new UserCredentialsDto()
                .email("USER@mail.com")
                .password("password")
        user = User.builder()
                .email("USER@mail.com")
                .build();
        userDto = new UserDto()
                .email("USER@mail.com")
                .name("User")
        passwordDto = new PasswordDto().password("newPassword")
    }

    void "correct credentials should lead to successful login"() {
        given: "correct credentials for existing USER"
            userService.authenticate(credentialsDto.email, credentialsDto.password) >>
                    Optional.ofNullable(user);

        and: "correct token for USER"
            userService.getUsersToken(user) >> "some_token";

        when: "login is invoked"
            ResponseEntity<AuthorizationTokenDto> responseEntity =
                    userApiDelegate.login(credentialsDto)

        then: "token is generated"
            responseEntity.body.token == "some_token"
    }

    void "incorrect credentials should lead to login failure"() {
        given: "correct credentials for existing USER"
        userService.authenticate(credentialsDto.email, credentialsDto.password) >>
                Optional.empty()

        when: "login is invoked"
        ResponseEntity<AuthorizationTokenDto> responseEntity =
                userApiDelegate.login(credentialsDto)

        then: "exception is thrown"
        thrown(UserNotFoundException)
    }

    void "correct user data should lead to successful registration"() {
        given: "correct data for non-existing USER"
        User mapped = UserMapper.getInstance().fromDto(userDto)
        userService.register(mapped) >> mapped

        when: "registration is invoked"
        ResponseEntity<Void> responseEntity = userApiDelegate.register(userDto)

        then: "USER is registered"
        responseEntity.statusCode.'2xxSuccessful'
    }

    void "incorrect user data should lead to registration failure"() {
        given: "data for existing USER"
        User mapped = UserMapper.getInstance().fromDto(userDto)
        userService.register(mapped) >> { throw new UserAlreadyExistsException() }

        when: "registration is invoked"
        ResponseEntity<Void> responseEntity = userApiDelegate.register(userDto)

        then: "exception is thrown"
        thrown(UserAlreadyExistsException)
    }

    void "existing user should change password successfully"() {
        given: "existing USER"
        userService.getAuthenticatedUser() >> user

        when: "changing password is invoked"
        ResponseEntity<Void> result = userApiDelegate.changePassword(passwordDto)

        then: "Password is changed"
        result.statusCode.'2xxSuccessful'
    }

    void "non-existing user should not be able to change password"() {
        given: "non-existing USER"
        userService.getAuthenticatedUser() >> { throw new IllegalStateException() }

        when: "getting changing password is invoked"
        ResponseEntity<Void> result = userApiDelegate.changePassword(passwordDto)

        then: "Exception is thrown"
        thrown(IllegalStateException.class)
    }

    void "existing user should deregister successfully"() {
        given: "existing USER"
        userService.getAuthenticatedUser() >> user

        when: "changing password is invoked"
        ResponseEntity<Void> result = userApiDelegate.deregister()

        then: "Password is changed"
        result.statusCode.'2xxSuccessful'
    }

    void "non-existing user should not be able to deregister"() {
        given: "non-existing USER"
        userService.getAuthenticatedUser() >> { throw new IllegalStateException() }

        when: "getting changing password is invoked"
        ResponseEntity<Void> result = userApiDelegate.deregister()

        then: "Exception is thrown"
        thrown(IllegalStateException.class)
    }

    void "existing user should get his details successfully"() {
        given: "existing USER"
        userService.getAuthenticatedUser() >> user

        when: "getting details is invoked"
        ResponseEntity<UserDto> result = userApiDelegate.getMyDetails()

        then: "status is successful"
        result.statusCode.'2xxSuccessful'

        and: "body is USER's dto"
        result.body == UserMapper.getInstance().toDto(user)
    }

    void "non-existing user should not get his details"() {
        given: "invalid USER"
        userService.getAuthenticatedUser() >> { throw new IllegalStateException() }

        when: "getting changing password is invoked"
        ResponseEntity<UserDto> result = userApiDelegate.getMyDetails()

        then: "Exception is thrown"
        thrown(IllegalStateException.class)
    }

    void "existing user should request reset of his password"() {
        given: "existing USER"
        userService.requestPasswordReset(credentialsDto.email) >> { }

        when: "password reset is invoked"
        ResponseEntity<Void> result = userApiDelegate.requestPasswordReset(credentialsDto)

        then: "status is successful"
        result.statusCode.'2xxSuccessful'
    }

    void "non-existing user should not request reset of  his password"() {
        given: "non-existing USER"
        userService.requestPasswordReset(credentialsDto.email) >> { throw new UserNotFoundException() }

        when: "password change is invoked"
        ResponseEntity<Void> result = userApiDelegate.requestPasswordReset(credentialsDto)

        then: "Exception is thrown"
        thrown(UserNotFoundException.class)
    }
}
