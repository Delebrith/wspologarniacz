package com.purplepanda.wspologarniacz.task;

import com.purplepanda.wspologarniacz.user.User;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @ManyToMany
    @JoinTable(name = "authorized", inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> authorized;

}
