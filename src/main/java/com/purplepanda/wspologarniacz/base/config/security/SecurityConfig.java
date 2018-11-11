package com.purplepanda.wspologarniacz.base.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity
class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final String[] SWAGGER_ANT_PATTERNS = {
            "/v2/api-docs", "/configuration/ui", "/swagger-resources/**", "/configuration/**", "/swagger-ui.html", "/webjars/**",
    };

    private static final String[] PUBLIC_ANT_PATTERNS = {
            "/public/**", "/user/login", "/user/register/**", "/user/password/reset/**",
            "/", "/favicon.ico"
    };

    private final UserDetailsService userDetailsService;
    private final String secretKey;

    public SecurityConfig(UserDetailsService userServiceImpl, String secretKey) {
        this.userDetailsService = userServiceImpl;
        this.secretKey = secretKey;
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .authorizeRequests().antMatchers("/h2/**").permitAll().and()
                .authorizeRequests().antMatchers(SWAGGER_ANT_PATTERNS).permitAll().and()
                .authorizeRequests().antMatchers(PUBLIC_ANT_PATTERNS).permitAll().and()
                .authorizeRequests().anyRequest().authenticated()
                .and()
                .addFilter(jwtAuthorizationFilter())
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        httpSecurity
                .csrf().disable();
        httpSecurity
                .headers().frameOptions().disable();
    }

    @Override
    protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(new BCryptPasswordEncoder());
    }

    @Bean
    protected JWTAuthorizationFilter jwtAuthorizationFilter() throws Exception {
        return new JWTAuthorizationFilter(authenticationManager(), secretKey, userDetailsService);
    }
}
