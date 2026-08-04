package com.powerfault.backend.controller;

import com.powerfault.backend.entity.Incident;
import com.powerfault.backend.repository.IncidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/incidents")
@RequiredArgsConstructor
public class IncidentController {

    private final IncidentRepository incidentRepository;

    @GetMapping
    public ResponseEntity<List<Incident>> getAllIncidents() {
        return ResponseEntity.ok(incidentRepository.findAll());
    }
}
