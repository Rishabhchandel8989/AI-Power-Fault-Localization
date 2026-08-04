package com.powerfault.backend.service;

import com.powerfault.backend.dto.response.AiBriefingResponse;

public interface AiBriefingService {
    AiBriefingResponse generateBriefing(Long incidentId);
}
