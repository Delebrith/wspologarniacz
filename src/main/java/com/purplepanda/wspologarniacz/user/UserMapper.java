package com.purplepanda.wspologarniacz.user;

import com.purplepanda.wspologarniacz.api.model.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    static UserMapper getInstance() {
        return Mappers.getMapper(UserMapper.class);
    }

    @Mapping(target = "active", ignore = true)
    @Mapping(target = "id", ignore = true)
    User fromDto(UserDto dto);

    @Mapping(target = "password", constant = "***")
    UserDto toDto(User entity);

}
