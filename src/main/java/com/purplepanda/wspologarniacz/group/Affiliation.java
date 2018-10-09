package com.purplepanda.wspologarniacz.group;

import com.purplepanda.wspologarniacz.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "group_id"}))
class Affiliation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    private AffiliationState state;

    @NotNull
    @Column(name = "LAST_UPDATED")
    private LocalDateTime lastUpdated;


    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @PrimaryKeyJoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

}
