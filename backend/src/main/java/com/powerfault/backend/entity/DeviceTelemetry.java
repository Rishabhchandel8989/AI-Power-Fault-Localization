package com.powerfault.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "device_telemetry")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceTelemetry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Current energized state.
     */
    private Boolean energized;

    /**
     * Latest event received.
     *
     * heartbeat
     * power_lost
     * power_restored
     * boot
     */
    private String event;

    private Integer sequenceNumber;

    private Integer batteryMv;

    private Integer rssi;

    private LocalDateTime timestamp;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id",
            nullable = false,
            unique = true)
    private Device device;
}