package com.purplepanda.wspologarniacz.user;

import org.springframework.security.core.GrantedAuthority;

enum AuthorityName implements GrantedAuthority {
    ADMIN, USER;

    @Override
    public String getAuthority() {
        return this.toString();
    }
}
