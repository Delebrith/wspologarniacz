package com.purplepanda.wspologarniacz.ranking;

import com.purplepanda.wspologarniacz.user.User;
import com.purplepanda.wspologarniacz.user.authorization.ModifiableResource;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Ranking extends ModifiableResource {

    @NotBlank
    private String name;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "ranking_id")
    @Builder.Default
    private Set<Category> categories = new HashSet<>();

}
