package com.purplepanda.wspologarniacz.user;

import org.springframework.data.repository.CrudRepository;

public interface RequestRepository extends CrudRepository<Token, Long> {
}
