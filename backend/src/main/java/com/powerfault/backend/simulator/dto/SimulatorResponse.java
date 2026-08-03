package com.powerfault.backend.simulator.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulatorResponse {

    private boolean success;

    private String message;

}