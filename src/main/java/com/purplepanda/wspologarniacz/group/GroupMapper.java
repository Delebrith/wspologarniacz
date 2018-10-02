package com.purplepanda.wspologarniacz.group;

import com.purplepanda.wspologarniacz.api.model.AffiliationDto;
import com.purplepanda.wspologarniacz.api.model.AffiliationTypeDto;
import com.purplepanda.wspologarniacz.api.model.GroupDto;
import com.purplepanda.wspologarniacz.user.UserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GroupMapper {

    static GroupMapper getInstance() {
        return Mappers.getMapper(GroupMapper.class);
    }

    @Mapping(target = "affiliations", ignore = true)
    @Mapping(target = "id", ignore = true)
    Group fromDto(GroupDto dto);

    GroupDto toDto(Group entity);

    default AffiliationDto toDto(Affiliation entity) {
        return new AffiliationDto()
                .lastUpdated(OffsetDateTime.of(entity.getLastUpdated(), ZoneOffset.ofHours(1)))
                .state(AffiliationTypeDto.fromValue(entity.getState().name()))
                .user(UserMapper.getInstance().toDto(entity.getUser()));
    }
}
