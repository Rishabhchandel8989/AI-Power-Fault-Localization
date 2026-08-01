//package com.powerfault.backend.entity;
//
//public class Feeder {
//}
package com.powerfault.backend.entity;

import com.powerfault.backend.enums.FeederStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "feeders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Feeder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String feederCode;

    @Column(nullable = false)
    private String feederName;

    @Enumerated(EnumType.STRING)
    private FeederStatus status;

    @OneToMany(mappedBy = "feeder",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<Transformer> transformers = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "substation_id", nullable = false)
    private Substation substation;
}