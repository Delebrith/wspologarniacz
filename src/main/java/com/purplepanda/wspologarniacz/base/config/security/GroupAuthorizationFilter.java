package com.purplepanda.wspologarniacz.base.config.security;

import com.purplepanda.wspologarniacz.group.exception.NotGroupMemberException;
import com.purplepanda.wspologarniacz.user.User;
import com.purplepanda.wspologarniacz.user.authorization.ResourceType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class GroupAuthorizationFilter extends OncePerRequestFilter implements ResourceAuthorizationFilter {

    private final UserDetailsService userDetailsService;

    public GroupAuthorizationFilter(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        User authenticated = (User) userDetailsService.loadUserByUsername(request.getUserPrincipal().getName());
        String elements[] = request.getContextPath().split("/");
        handleResource(authenticated, Long.getLong(elements[1]));
        filterChain.doFilter(request, response);
    }

    @Override
    public void handleResource(User authenticated, Long resourceId) {
        authenticated.getModifiableResources().stream()
                .filter(m -> m.getResourceType().equals(ResourceType.GROUP))
                .filter(m -> m.getResourceId().equals(resourceId))
                .findFirst()
                .orElseThrow(NotGroupMemberException::new);
    }

}
