package com.purplepanda.wspologarniacz.schedule;

import com.purplepanda.wspologarniacz.user.authorization.ModifiableResource;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.time.LocalTime;
import java.time.Period;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@PrimaryKeyJoinColumn(name = "id")
public class Schedule extends ModifiableResource {

    @NotBlank
    private String name;

    private Integer counter;

    @Column
    @Convert(converter = PeriodConverter.class)
    private Period period;

    private LocalTime reminderTime;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "schedule_id")
    private Set<Ordinal> order;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "schedule_id")
    @Builder.Default
    private Set<HistoryRecord> history = new HashSet<>();

}

