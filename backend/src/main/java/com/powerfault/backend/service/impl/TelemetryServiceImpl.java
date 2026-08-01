package com.powerfault.backend.service.impl;

import com.powerfault.backend.dto.request.TelemetryRequest;
import com.powerfault.backend.entity.Device;
import com.powerfault.backend.entity.DeviceTelemetry;
import com.powerfault.backend.repository.DeviceRepository;
import com.powerfault.backend.repository.DeviceTelemetryRepository;
import com.powerfault.backend.repository.TelemetryHistoryRepository;
import com.powerfault.backend.service.TelemetryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TelemetryServiceImpl implements TelemetryService {

    private final DeviceRepository deviceRepository;

    private final DeviceTelemetryRepository deviceTelemetryRepository;

    private final TelemetryHistoryRepository telemetryHistoryRepository;

    @Override
    public DeviceTelemetry processTelemetry(TelemetryRequest request) {

        Device device = deviceRepository
                .findByDeviceId(request.getDeviceId())
                .orElseThrow(() ->
                        new RuntimeException("Device not found : "
                                + request.getDeviceId()));

        return null;
    }
}