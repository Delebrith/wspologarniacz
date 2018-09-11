package com.purplepanda.wspologarniacz.group;

import com.purplepanda.wspologarniacz.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
class Affiliation {

    @NotBlank
    @Enumerated(EnumType.STRING)
    private AffiliationState state;

    @OneToMany
    private User user;
}
