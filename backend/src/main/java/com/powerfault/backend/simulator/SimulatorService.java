package com.powerfault.backend.simulator;

import java.util.Map;

public interface SimulatorService {

    Map<String, Object> simulateSpanFault(String poleCode);

    Map<String, Object> simulateTransformerFault(String transformerCode);

    Map<String, Object> simulateFeederFault(String feederCode);

    Map<String, Object> simulateDeviceFailure(String poleCode);

    Map<String, Object> simulateScheduledOutage(String scope, String targetId, String reason);

    Map<String, Object> repairFault(String poleCode);

    Map<String, Object> resetNetwork();
}