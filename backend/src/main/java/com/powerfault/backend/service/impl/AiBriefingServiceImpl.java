package com.powerfault.backend.service.impl;

import com.powerfault.backend.dto.response.AiBriefingResponse;
import com.powerfault.backend.entity.Incident;
import com.powerfault.backend.enums.FaultType;
import com.powerfault.backend.repository.IncidentRepository;
import com.powerfault.backend.service.AiBriefingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiBriefingServiceImpl implements AiBriefingService {

    private final IncidentRepository incidentRepository;

    @Override
    public AiBriefingResponse generateBriefing(Long incidentId) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new RuntimeException("Incident not found: " + incidentId));

        FaultType faultType = incident.getFaultType();
        String fromPole = incident.getFromPole() != null ? incident.getFromPole().getPoleCode() : "SUBSTATION/DT";
        String toPole = incident.getToPole() != null ? incident.getToPole().getPoleCode() : "UNKNOWN";
        int affected = incident.getAffectedPoleCount() != null ? incident.getAffectedPoleCount() : 1;
        double confidence = incident.getConfidence() != null ? incident.getConfidence() : 80.0;

        String severity;
        int recommendedCrew;
        List<String> materials = new ArrayList<>();
        List<String> directives = new ArrayList<>();
        String summary;
        String locationDetails;
        String topologyNotes;

        if (faultType == FaultType.FEEDER_FAULT) {
            severity = "CRITICAL - FEEDER LEVEL OUTAGE";
            recommendedCrew = 4;
            summary = String.format("Complete 11kV Feeder trip affecting all downstream distribution transformers under this feeder. High risk of wide area customer outage.", fromPole);
            materials.add("11kV Vacuum Circuit Breaker Spares");
            materials.add("Substation Protection Relay Kit");
            materials.add("High Voltage Insulated Gloves & Arc Flash Suit");
            directives.add("Confirm feeder breaker status at Substation SS-001");
            directives.add("Perform insulation resistance test before reclosing feeder breaker");
        } else if (faultType == FaultType.TRANSFORMER_FAULT) {
            severity = "HIGH - DISTRIBUTION TRANSFORMER OUTAGE";
            recommendedCrew = 3;
            summary = String.format("Distribution Transformer fault detected. All poles under transformer %s are de-energized. High probability of blown HT Drop Out Fuse or internal coil fault.", toPole);
            materials.add("11kV HT Drop Out Fuse Elements (10A/15A)");
            materials.add("Transformer Oil Testing Meter & Top-up Can");
            materials.add("11m Telescopic Operating Rod");
            directives.add("Open 11kV DO Fuse isolator before approaching transformer structure");
            directives.add("Inspect LT busbar for thermal discoloration or physical snap");
        } else { // SPAN_FAULT
            severity = "HIGH - LT SPAN FAILURE";
            recommendedCrew = 2;
            summary = String.format("Physical LT wire snap or blown jumper detected on the span between energised pole %s and dark pole %s. %d downstream poles are isolated.", fromPole, toPole, affected);
            materials.add("50m 3-Phase 4-Wire ACSR Conductor (LT)");
            materials.add("PCC Pole Climbing Ladder & Safety Belts");
            materials.add("PG Clamps & Tension Dead-End Fittings");
            materials.add("LT Fuse Wire (100A)");
            directives.add("Isolate DT LT breaker or remove feeder fuses prior to span repair");
            directives.add("Verify zero-voltage using non-contact detector at pole " + toPole);
            directives.add("Ensure clearance from low-hanging telephone cables during wire pulling");
        }

        locationDetails = String.format("Span: %s ➔ %s | GPS: %.6f, %.6f | PIN: %s",
                fromPole, toPole,
                incident.getLatitude() != null ? incident.getLatitude() : 0.0,
                incident.getLongitude() != null ? incident.getLongitude() : 0.0,
                incident.getPincode() != null ? incident.getPincode() : "560078");

        if (confidence >= 90.0) {
            topologyNotes = "High Confidence (95%): Network topology is fully mapped (1:1 parent-child pole records). Span boundaries are exact.";
        } else {
            topologyNotes = "Medium Confidence (65%): Network topology for this DT is unmapped in GIS (60% case). Span location inferred via geometric proximity. Lineman must physically inspect adjacent poles.";
        }

        int estimatedHouseholds = affected * 18; // approx 18 households per pole drop

        return AiBriefingResponse.builder()
                .incidentId(incident.getId())
                .incidentNumber(incident.getIncidentNumber())
                .severity(severity)
                .summary(summary)
                .locationDetails(locationDetails)
                .estimatedHouseholdImpact(estimatedHouseholds)
                .recommendedCrewSize(recommendedCrew)
                .materialsChecklist(materials)
                .safetyDirectives(directives)
                .topologyNotes(topologyNotes)
                .build();
    }
}
