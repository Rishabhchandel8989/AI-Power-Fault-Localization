//package com.powerfault.backend.entity;
//
//public class Transformer {
//}

package com.powerfault.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "transformers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transformer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String transformerCode;

    @Column(nullable = false)
    private String transformerName;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    /**
     * Indicates whether the pole ordering
     * for this transformer is available.
     *
     * Around 60% will be false
     * in the simulator.
     */
    private Boolean topologyKnown;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feeder_id", nullable = false)
    private Feeder feeder;

    @OneToMany(
            mappedBy = "transformer",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<Pole> poles = new ArrayList<>();
}