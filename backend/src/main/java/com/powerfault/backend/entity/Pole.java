//package com.powerfault.backend.entity;
//
//public class Pole {
//}


package com.powerfault.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "poles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String poleCode;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    /**
     * Pole ordering under a transformer.
     * Can be null when topology is unknown.
     */
    private Integer sequenceNumber;

    /**
     * Around 9% of poles will not
     * have an IoT device.
     */
    @Column(nullable = false)
    private Boolean hasDevice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transformer_id", nullable = false)
    private Transformer transformer;

    @OneToOne(
            mappedBy = "pole",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private Device device;
}