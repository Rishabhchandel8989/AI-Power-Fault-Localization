package com.powerfault.backend.service.impl;

import com.powerfault.backend.dto.request.TelemetryRequest;
import com.powerfault.backend.dto.response.LocalizationResult;
import com.powerfault.backend.repository.*;
import com.powerfault.backend.service.LocalizationService;
import com.powerfault.backend.service.TelemetryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocalizationServiceImpl implements LocalizationService {

    private final PoleRepository poleRepository;

    private final DeviceRepository deviceRepository;

    private final DeviceTelemetryRepository deviceTelemetryRepository;

    private final IncidentRepository incidentRepository;

    private final NetworkConnectionRepository networkConnectionRepository;

    private final TelemetryService telemetryService;

    @Override
    public LocalizationResult localizeFault(TelemetryRequest request) {

        telemetryService.processTelemetry(request);

        return LocalizationResult.builder()
                .confidence(0.0)
                .reasoning("Telemetry stored successfully. Localization pending.")
                .build();
    }
}
