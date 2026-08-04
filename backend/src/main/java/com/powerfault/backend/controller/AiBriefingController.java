package com.powerfault.backend.controller;

import com.powerfault.backend.dto.response.AiBriefingResponse;
import com.powerfault.backend.service.AiBriefingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/incidents")
@RequiredArgsConstructor
public class AiBriefingController {

    private final AiBriefingService aiBriefingService;

    @GetMapping("/{id}/ai-brief")
    public ResponseEntity<AiBriefingResponse> getAiBriefing(@PathVariable Long id) {
        AiBriefingResponse briefing = aiBriefingService.generateBriefing(id);
        return ResponseEntity.ok(briefing);
    }
}
