package com.purplepanda.wspologarniacz.group;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

public interface GroupRepository extends CrudRepository<Group, Long> {
    Page<Group> findByNameIgnoreCaseContaining(String name, Pageable pageable);
}
