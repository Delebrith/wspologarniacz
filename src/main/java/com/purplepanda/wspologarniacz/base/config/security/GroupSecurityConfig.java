package com.purplepanda.wspologarniacz.base.config.security;

import com.purplepanda.wspologarniacz.user.authorization.ResourceType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class GroupSecurityConfig extends WebSecurityConfigurerAdapter {


    private static final String[] PROTECTED_GROUP_ANT_PATTENRS = {
            "/group/{groupId}/invite**", "/group/{groupId}/leave**", "/group/{groupId}/accept**", "/group/{groupId}/reject**",
            "/group/{groupId}/task**", "/group/{groupId}/ranking**"
    };

    private final UserDetailsService userDetailsService;

    public GroupSecurityConfig(UserDetailsService userServiceImpl) {
        this.userDetailsService = userServiceImpl;
    }


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers(PROTECTED_GROUP_ANT_PATTENRS)
                .authenticated()
                .and()
                .addFilterAfter(resourceAuthorizationFilterFactory(userDetailsService).getFilter(ResourceType.GROUP), BasicAuthenticationFilter.class);
    }

    @Bean
    protected ResourceAuthorizationFilterFactory resourceAuthorizationFilterFactory(final UserDetailsService userServiceImpl) {
        return new ResourceAuthorizationFilterFactory(userServiceImpl);
    }


}
