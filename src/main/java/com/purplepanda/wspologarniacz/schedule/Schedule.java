package com.purplepanda.wspologarniacz.schedule;

import com.purplepanda.wspologarniacz.user.authorization.ModifiableResource;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.validation.constraints.NotBlank;

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

    
}

