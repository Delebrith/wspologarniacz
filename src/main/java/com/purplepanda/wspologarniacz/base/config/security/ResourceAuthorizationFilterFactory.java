package com.purplepanda.wspologarniacz.base.config.security;

import com.purplepanda.wspologarniacz.user.authorization.ResourceType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

class ResourceAuthorizationFilterFactory {

    private final UserDetailsService userDetailsService;

    ResourceAuthorizationFilterFactory(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    public OncePerRequestFilter getFilter(ResourceType resourceType){
        if (resourceType.equals(ResourceType.GROUP))
            return new GroupAuthorizationFilter(userDetailsService);
        return null;
    }
}
