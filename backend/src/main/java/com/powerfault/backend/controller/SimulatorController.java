package com.powerfault.backend.controller;

import com.powerfault.backend.simulator.SimulatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/simulator")
@RequiredArgsConstructor
public class SimulatorController {

    private final SimulatorService simulatorService;

    @PostMapping("/span/{poleCode}")
    public String span(@PathVariable String poleCode) {

        simulatorService.simulateSpanFault(poleCode);

        return "Span fault injected.";

    }

    @PostMapping("/transformer/{transformerCode}")
    public String transformer(@PathVariable String transformerCode) {

        simulatorService.simulateTransformerFault(transformerCode);

        return "Transformer fault injected.";

    }

    @PostMapping("/feeder/{feederCode}")
    public String feeder(@PathVariable String feederCode) {

        simulatorService.simulateFeederFault(feederCode);

        return "Feeder fault injected.";

    }

    @PostMapping("/device/{deviceId}")
    public String device(@PathVariable String deviceId) {

        simulatorService.simulateDeviceFailure(deviceId);

        return "Device failure simulated.";

    }

    @PostMapping("/repair/{poleCode}")
    public String repair(@PathVariable String poleCode) {

        simulatorService.repairFault(poleCode);

        return "Fault repaired.";

    }

}