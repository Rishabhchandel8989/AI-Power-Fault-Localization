package com.powerfault.backend.entity;


import com.powerfault.backend.entity.Pole;
import jakarta.persistence.*;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "network_connections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NetworkConnection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_pole_id", nullable = false)
    private Pole fromPole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_pole_id", nullable = false)
    private Pole toPole;

    private Double distanceMeters;

    @Builder.Default
    private Boolean active = true;
}