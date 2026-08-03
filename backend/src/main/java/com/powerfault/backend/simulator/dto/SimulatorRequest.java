package com.powerfault.backend.simulator.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulatorRequest {

    private String faultType;

    private String targetId;

}