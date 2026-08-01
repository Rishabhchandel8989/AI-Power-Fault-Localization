package com.powerfault.backend.service;

import com.powerfault.backend.dto.request.TelemetryRequest;
import com.powerfault.backend.entity.DeviceTelemetry;

public interface TelemetryService {

    DeviceTelemetry processTelemetry(TelemetryRequest request);

}