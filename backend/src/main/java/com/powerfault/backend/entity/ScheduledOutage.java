package com.powerfault.backend.entity;

import com.powerfault.backend.enums.OutageScope;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "scheduled_outages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduledOutage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String outageId;

    @Enumerated(EnumType.STRING)
    private OutageScope scope;

    @Column(nullable = false)
    private String targetId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String reason;
}