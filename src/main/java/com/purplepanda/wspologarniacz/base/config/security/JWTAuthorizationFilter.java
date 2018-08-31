package com.purplepanda.wspologarniacz.base.config.security;

import com.purplepanda.wspologarniacz.user.User;
import io.jsonwebtoken.Jwts;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Optional;

class JWTAuthorizationFilter extends BasicAuthenticationFilter {
    private static final String AUTHORIZATION_SCHEMA = "bearer ";

    private final byte[] secretKey;
    private final UserDetailsService userServiceImpl;

    JWTAuthorizationFilter(
            final AuthenticationManager authenticationManager, String secretKey, UserDetailsService userServiceImpl) {
        super(authenticationManager);
        this.secretKey = secretKey.getBytes(Charset.forName("UTF-8"));
        this.userServiceImpl = userServiceImpl;
    }

    @Override
    protected void doFilterInternal(
            final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain)
            throws IOException, ServletException {
        Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION))
                .ifPresent(this::handleAuthorizationHeader);
        chain.doFilter(request, response);
    }

    private void handleAuthorizationHeader(final String header) {
        if (!header.toLowerCase().startsWith(AUTHORIZATION_SCHEMA)) {
            return;
        }

        try {
            final String subject = Jwts.parser()
                            .setSigningKey(secretKey)
                            .parseClaimsJws(header.split(" ")[1])
                            .getBody()
                            .getSubject();

            if (null != subject) {
                User user = (User) userServiceImpl.loadUserByUsername(subject);
                if (user.isEnabled()) {
                    SecurityContextHolder.getContext()
                            .setAuthentication(
                                    new UsernamePasswordAuthenticationToken(subject, null, user.getAuthorities()));
                }
            }
        } catch (final Exception e) {
            // Ignore invalid JWT
        }
    }
}
