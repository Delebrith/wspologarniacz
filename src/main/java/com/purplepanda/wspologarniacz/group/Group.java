package com.purplepanda.wspologarniacz.group;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "GROUP_")
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @Builder.Default
    @ElementCollection(targetClass = Affiliation.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "AFFILIATIONS", joinColumns = @JoinColumn(name = "GROUP_ID", referencedColumnName = "ID"))
    @Column(name = "AFFILIATIONS")
    private Set<Affiliation> affiliations = new HashSet<>();
}
