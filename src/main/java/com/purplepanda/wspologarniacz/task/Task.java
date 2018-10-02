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
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    @NotNull
    private LocalDateTime updateTime;

    @ManyToOne
    private User lastModifiedBy;

    @ManyToMany
    private Set<User> authorized;

}
