package com.purplepanda.wspologarniacz.task;

import com.purplepanda.wspologarniacz.api.model.TaskDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TaskMapper {

    static TaskMapper getInstance() {
        return Mappers.getMapper(TaskMapper.class);
    }

    Task fromDto(TaskDto dto);

    TaskDto toDto(Task entity);
}
