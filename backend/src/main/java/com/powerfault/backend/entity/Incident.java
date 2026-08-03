package com.powerfault.backend.entity;

import com.powerfault.backend.enums.FaultType;
import com.powerfault.backend.enums.IncidentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "incidents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Human readable incident number.
     * Example:
     * INC-20260801-001
     */
    @Column(nullable = false, unique = true)
    private String incidentNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FaultType faultType;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private IncidentStatus status = IncidentStatus.DETECTED;

    /**
     * Between these two poles
     * the fault most likely exists.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_pole_id")
    private Pole fromPole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_pole_id")
    private Pole toPole;

    /**
     * GPS shown on the map.
     */
    private Double latitude;

    private Double longitude;

    /**
     * Required by assignment.
     */
    private String pincode;

    /**
     * Required by assignment.
     */
    private Integer affectedPoleCount;

    /**
     * 0-100%
     */
    private Double confidence;

    /**
     * Human explanation shown to operator.
     */
    @Column(length = 1000)
    private String reasoning;

    private LocalDateTime detectedAt;

    private LocalDateTime verifiedAt;

    private LocalDateTime closedAt;

    @PrePersist
    public void prePersist() {

        detectedAt = LocalDateTime.now();

    }
}