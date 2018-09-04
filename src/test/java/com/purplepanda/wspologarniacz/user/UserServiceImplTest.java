package com.purplepanda.wspologarniacz.user;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest
public class UserServiceImplTest {

    private static final String USERNAME = "USERNAME@mail.com";
    private static final User USER = User.builder()
                                .email(USERNAME)
                                .password("password")
                                .build();

    @Mock
    private UserRepository userRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private RequestTokenRepository requestTokenRepository;
    private String secret = "super_secret";
    private String serverUrl = "http://localhost:8080";

    UserServiceImpl userService;

    @Before
    public void init() {
        userService = new UserServiceImpl(userRepository, requestTokenRepository, secret, eventPublisher, serverUrl);
    }

    @WithMockUser(USERNAME)
    @Test
    public void getExistingAuthenticatedUser() {
        //given
        Mockito.when(userRepository.findByEmail(USERNAME)).thenReturn(Optional.of(USER));
        //when
        User authenticated = userService.getAuthenticatedUser();
        //then
        authenticated.equals(USER);
    }

    @Test(expected = IllegalStateException.class)
    public void getNonExistingAuthenticatedUser() {
        //given
        Mockito.when(userRepository.findByEmail(USERNAME)).thenReturn(Optional.empty());
        //when
        userService.getAuthenticatedUser();
    }

}
