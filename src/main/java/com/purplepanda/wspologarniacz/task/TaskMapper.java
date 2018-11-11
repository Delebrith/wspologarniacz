package com.purplepanda.wspologarniacz.task;

import com.purplepanda.wspologarniacz.api.model.TaskDto;
import com.purplepanda.wspologarniacz.api.model.TaskInfoDto;
import com.purplepanda.wspologarniacz.api.model.TaskStatusDto;
import com.purplepanda.wspologarniacz.user.User;
import com.purplepanda.wspologarniacz.user.UserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TaskMapper {

    static TaskMapper getInstance() {
        return Mappers.getMapper(TaskMapper.class);
    }
    static UserMapper getUserMapper() { return Mappers.getMapper(UserMapper.class); }

    @Mapping(target = "updateTime", ignore = true)
    Task fromDto(TaskDto dto);

    default TaskDto toDto(Task entity) {
        return new TaskDto()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .status(TaskStatusDto.fromValue(entity.getStatus().name()))
                .lastModifiedBy(getUserMapper().toDto(entity.getLastModifiedBy()))
                .updateTime(OffsetDateTime.of(entity.getUpdateTime(), ZoneOffset.ofHours(1)));
    }

    default Task toEntity(TaskInfoDto dto, User authenticated) {
        return Task.builder()
                .description(dto.getDescription())
                .name(dto.getName())
                .status(TaskStatus.ADDED)
                .lastModifiedBy(authenticated)
                .updateTime(LocalDateTime.now())
                .build();
    }
}
