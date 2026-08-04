package com.powerfault.backend.controller;

import com.powerfault.backend.simulator.SimulatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/simulator")
@RequiredArgsConstructor
public class SimulatorController {

    private final SimulatorService simulatorService;

    @PostMapping("/span/{poleCode}")
    public ResponseEntity<Map<String, Object>> span(@PathVariable String poleCode) {
        return ResponseEntity.ok(simulatorService.simulateSpanFault(poleCode));
    }

    @PostMapping("/transformer/{transformerCode}")
    public ResponseEntity<Map<String, Object>> transformer(@PathVariable String transformerCode) {
        return ResponseEntity.ok(simulatorService.simulateTransformerFault(transformerCode));
    }

    @PostMapping("/feeder/{feederCode}")
    public ResponseEntity<Map<String, Object>> feeder(@PathVariable String feederCode) {
        return ResponseEntity.ok(simulatorService.simulateFeederFault(feederCode));
    }

    @PostMapping("/device/{poleCode}")
    public ResponseEntity<Map<String, Object>> device(@PathVariable String poleCode) {
        return ResponseEntity.ok(simulatorService.simulateDeviceFailure(poleCode));
    }

    @PostMapping("/outage")
    public ResponseEntity<Map<String, Object>> outage(@RequestBody Map<String, String> body) {
        String scope = body.getOrDefault("scope", "feeder");
        String targetId = body.getOrDefault("targetId", "F-07-01");
        String reason = body.getOrDefault("reason", "Planned load shedding");
        return ResponseEntity.ok(simulatorService.simulateScheduledOutage(scope, targetId, reason));
    }

    @PostMapping("/repair/{poleCode}")
    public ResponseEntity<Map<String, Object>> repair(@PathVariable String poleCode) {
        return ResponseEntity.ok(simulatorService.repairFault(poleCode));
    }

    @PostMapping("/reset")
    public ResponseEntity<Map<String, Object>> reset() {
        return ResponseEntity.ok(simulatorService.resetNetwork());
    }
}