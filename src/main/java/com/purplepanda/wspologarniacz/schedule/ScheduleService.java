package com.purplepanda.wspologarniacz.schedule;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ScheduleService {
    Schedule getSchedule(Long id);

    void deleteSchedule(Schedule schedule);

    Schedule submitAction(Schedule schedule);

    Schedule modifySchedule(Optional<String> name, Optional<LocalTime> reminderTime);

    Schedule reorderSchedule(List<Ordinal> order);
}
