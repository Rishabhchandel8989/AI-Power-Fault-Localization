package com.powerfault.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "substations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Substation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String substationCode;

    @Column(nullable = false)
    private String substationName;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @OneToMany(
            mappedBy = "substation",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<Feeder> feeders = new ArrayList<>();
}