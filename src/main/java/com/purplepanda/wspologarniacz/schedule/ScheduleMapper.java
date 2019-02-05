package com.purplepanda.wspologarniacz.schedule;

import com.purplepanda.wspologarniacz.api.model.HistoryRecordDto;
import com.purplepanda.wspologarniacz.api.model.OrdinalDto;
import com.purplepanda.wspologarniacz.api.model.ScheduleDto;
import com.purplepanda.wspologarniacz.user.UserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.time.*;
import java.util.stream.Collectors;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ScheduleMapper {

    static ScheduleMapper getInstance() {
        return Mappers.getMapper(ScheduleMapper.class);
    }

    default ScheduleDto toDto(Schedule entity) {
        return new ScheduleDto()
                .id(entity.getId())
                .name(entity.getName())
                .period(entity.getPeriod().toString())
                .counter(entity.getCounter())
                .order(entity.getOrder().stream()
                    .map(ordinal -> getInstance().toDto(ordinal))
                    .collect(Collectors.toList()))
                .reminderTime(entity.getReminderTime().toString());
    }

    default Schedule fromDto(ScheduleDto dto) {
        return Schedule.builder()
                .name(dto.getName())
                .counter(dto.getCounter())
                .period(Period.parse(dto.getPeriod()))
                .reminderTime(LocalTime.parse(dto.getReminderTime()))
                .order(dto.getOrder().stream()
                    .map(ordinal -> getInstance().fromDto(ordinal))
                    .collect(Collectors.toSet()))
                .build();
    }

    OrdinalDto toDto(Ordinal entity);

    Ordinal fromDto(OrdinalDto dto);

    default HistoryRecordDto toDto(HistoryRecord entity) {
        return new HistoryRecordDto()
                .user(UserMapper.getInstance().toDto(entity.getUser()))
                .updateTime(OffsetDateTime.of(entity.getUpdateTime(), ZoneOffset.ofHours(1)));
    }

    default HistoryRecord fromDto(HistoryRecordDto dto){
        return HistoryRecord.builder()
                .user(UserMapper.getInstance().fromDto(dto.getUser()))
                .updateTime(dto.getUpdateTime().toLocalDateTime())
                .build();
    }
}
