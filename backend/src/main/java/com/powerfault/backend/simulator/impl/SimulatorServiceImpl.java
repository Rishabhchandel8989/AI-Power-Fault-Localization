package com.powerfault.backend.simulator.impl;

import com.powerfault.backend.repository.PoleRepository;
import com.powerfault.backend.service.LocalizationService;
import com.powerfault.backend.service.graph.GraphTraversalService;
import com.powerfault.backend.simulator.SimulatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SimulatorServiceImpl implements SimulatorService {

    private final PoleRepository poleRepository;
    private final GraphTraversalService graphTraversalService;
    private final LocalizationService localizationService;

    @Override
    public void simulateSpanFault(String poleCode) {
        throw new UnsupportedOperationException(
                "Span fault simulation will be implemented after the localization engine is completed."
        );
    }

    @Override
    public void simulateTransformerFault(String transformerCode) {
        throw new UnsupportedOperationException(
                "Transformer fault simulation is not implemented yet."
        );
    }

    @Override
    public void simulateFeederFault(String feederCode) {
        throw new UnsupportedOperationException(
                "Feeder fault simulation is not implemented yet."
        );
    }

    @Override
    public void simulateDeviceFailure(String deviceId) {
        throw new UnsupportedOperationException(
                "Device failure simulation is not implemented yet."
        );
    }

    @Override
    public void repairFault(String poleCode) {
        throw new UnsupportedOperationException(
                "Repair simulation is not implemented yet."
        );
    }
}