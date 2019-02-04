package com.purplepanda.wspologarniacz.task;

import com.purplepanda.wspologarniacz.user.User;
import com.purplepanda.wspologarniacz.user.authorization.ModifiableResource;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@PrimaryKeyJoinColumn(name = "id")
public class Task extends ModifiableResource {

    @NotBlank
    private String name;

    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TaskStatus status = TaskStatus.ADDED;

    @NotNull
    private LocalDateTime updateTime;

    @ManyToOne
    @JoinColumn(name = "last_modified_by")
    private User lastModifiedBy;

}
