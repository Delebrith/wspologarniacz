package com.purplepanda.wspologarniacz.ranking;

import com.purplepanda.wspologarniacz.api.model.CategoryDto;
import com.purplepanda.wspologarniacz.api.model.RankingDto;
import com.purplepanda.wspologarniacz.api.model.ScoreDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RankingMapper {


    static RankingMapper getInstance() {
        return Mappers.getMapper(RankingMapper.class);
    }

    @Mapping(target = "id", ignore = true)
    Ranking fromDto(RankingDto dto);

    RankingDto toDto(Ranking entity);

    @Mapping(target = "id", ignore = true)
    Category fromDto(CategoryDto dto);

    CategoryDto toDto(Category entity);

    @Mapping(target = "id", ignore = true)
    Score fromDto(ScoreDto dto);

    @Mapping(target = "user.password", constant = "***")
    @Mapping(target = "user.authorities", ignore = true)
    ScoreDto toDto(Score entity);
}
