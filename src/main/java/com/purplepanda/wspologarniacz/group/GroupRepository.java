package com.purplepanda.wspologarniacz.group;

import com.purplepanda.wspologarniacz.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface GroupRepository extends CrudRepository<Group, Long> {
    Page<Group> findByNameIgnoreCaseContaining(String name, Pageable pageable);
    List<Group> findByAffiliationsUser(User user);
}
