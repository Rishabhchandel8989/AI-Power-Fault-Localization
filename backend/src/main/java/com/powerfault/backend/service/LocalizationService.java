package com.powerfault.backend.service;

import com.powerfault.backend.dto.request.TelemetryRequest;
import com.powerfault.backend.dto.response.LocalizationResult;

public interface LocalizationService {

    LocalizationResult localizeFault(TelemetryRequest request);

}