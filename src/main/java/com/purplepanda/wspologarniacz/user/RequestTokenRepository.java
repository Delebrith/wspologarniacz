package com.purplepanda.wspologarniacz.user;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface RequestTokenRepository extends CrudRepository<RequestToken, Long> {
    Optional<RequestToken> findByToken(String token);
}
