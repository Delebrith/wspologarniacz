package com.purplepanda.wspologarniacz.base.config.security;

import com.purplepanda.wspologarniacz.user.User;

public interface ResourceAuthorizationFilter {

    void handleResource(User authenticated, Long resourceId);
}
