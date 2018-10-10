package com.purplepanda.wspologarniacz.ranking;

import com.purplepanda.wspologarniacz.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Min;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Score {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Min(0)
    private Integer points;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @PrimaryKeyJoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
}
