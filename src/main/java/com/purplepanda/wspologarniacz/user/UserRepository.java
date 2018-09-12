package com.purplepanda.wspologarniacz.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

interface UserRepository extends CrudRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Page<User> findByNameIgnoreCaseContains(String name, Pageable pageable);
}
