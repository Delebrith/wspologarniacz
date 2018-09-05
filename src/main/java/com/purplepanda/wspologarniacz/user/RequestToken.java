package com.purplepanda.wspologarniacz.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
class RequestToken {

    // TODO
    //https://www.baeldung.com/spring-security-registration-i-forgot-my-password

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true)
    private String token;

    @ManyToOne
    private User requester;

    @NotNull
    private LocalDateTime expiresAt;
}
