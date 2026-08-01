package com.powerfault.backend.controller;

import com.powerfault.backend.dto.request.TelemetryRequest;
import com.powerfault.backend.dto.response.LocalizationResult;
import com.powerfault.backend.service.LocalizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/telemetry")
@RequiredArgsConstructor
public class TelemetryController {

    private final LocalizationService localizationService;

    @PostMapping
    public ResponseEntity<LocalizationResult> receiveTelemetry(
            @Valid @RequestBody TelemetryRequest request) {

        LocalizationResult result =
                localizationService.localizeFault(request);

        return ResponseEntity.ok(result);
    }
}