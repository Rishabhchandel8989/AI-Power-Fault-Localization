//package com.powerfault.backend.entity;
//
//public class Device {
//}
////

package com.powerfault.backend.entity;

import com.powerfault.backend.enums.DeviceStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "devices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String deviceId;

    @Column(nullable = false)
    private String firmwareVersion;

    private LocalDateTime lastSeen;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DeviceStatus status = DeviceStatus.UNKNOWN;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pole_id", nullable = false, unique = true)
    private Pole pole;


    @OneToOne(
            mappedBy = "device",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private DeviceTelemetry latestTelemetry;

    @OneToMany(
            mappedBy = "device",
            cascade = CascadeType.ALL
    )
    @Builder.Default
    private List<TelemetryHistory> telemetryHistory = new ArrayList<>();
}