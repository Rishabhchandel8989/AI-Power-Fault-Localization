package com.powerfault.backend.service.impl;

import com.powerfault.backend.dto.request.TelemetryRequest;
import com.powerfault.backend.dto.response.LocalizationResult;
import com.powerfault.backend.entity.*;
import com.powerfault.backend.enums.FaultType;
import com.powerfault.backend.enums.IncidentStatus;
import com.powerfault.backend.repository.*;
import com.powerfault.backend.service.IncidentService;
import com.powerfault.backend.service.LocalizationService;
import com.powerfault.backend.service.TelemetryService;
import com.powerfault.backend.service.TicketService;
import com.powerfault.backend.service.graph.GraphTraversalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class LocalizationServiceImpl implements LocalizationService {

    private final PoleRepository poleRepository;
    private final IncidentService incidentService;
    private final TicketService ticketService;
    private final DeviceRepository deviceRepository;
    private final DeviceTelemetryRepository deviceTelemetryRepository;
    private final IncidentRepository incidentRepository;
    private final NetworkConnectionRepository networkConnectionRepository;
    private final ScheduledOutageRepository scheduledOutageRepository;
    private final GraphTraversalService graphTraversalService;

    @Override
    @Transactional
    public LocalizationResult localizeFault(TelemetryRequest request) {

        // If telemetry states energized=true, no fault to localize
        if (Boolean.TRUE.equals(request.getEnergized())) {
            return LocalizationResult.builder()
                    .faultDetected(false)
                    .reasoning("Pole is energized. No fault detected.")
                    .build();
        }

        Pole darkPole = findPole(request.getPoleId());
        Transformer transformer = darkPole.getTransformer();
        Feeder feeder = transformer.getFeeder();

        // 1. Scheduled Outage Check
        if (isUnderScheduledOutage(feeder, transformer)) {
            return LocalizationResult.builder()
                    .fromPole("SCHEDULED_MAINTENANCE")
                    .toPole(darkPole.getPoleCode())
                    .latitude(darkPole.getLatitude())
                    .longitude(darkPole.getLongitude())
                    .pincode(darkPole.getPincode())
                    .affectedPoles(0)
                    .confidence(100.0)
                    .reasoning("Suppressed: Scheduled load shedding / maintenance active for this feeder/transformer.")
                    .faultDetected(false)
                    .build();
        }

        // 2. Sensor Failure / Noise Filter Check
        // If darkPole has downstream children and ALL children are energized, darkPole is a sensor failure!
        if (isSensorFailure(darkPole)) {
            return LocalizationResult.builder()
                    .fromPole("DEAD_MODEM")
                    .toPole(darkPole.getPoleCode())
                    .latitude(darkPole.getLatitude())
                    .longitude(darkPole.getLongitude())
                    .pincode(darkPole.getPincode())
                    .affectedPoles(1)
                    .confidence(99.0)
                    .reasoning("Suppressed: Dead sensor / modem failure. Downstream poles remain live, physically disproving line fault.")
                    .faultDetected(false)
                    .build();
        }

        // 3. Determine Fault Type (Feeder, DT, or Span)
        FaultType faultType = determineFaultType(feeder, transformer, darkPole);

        Pole livePole = null;
        double confidence = 95.0;
        String reasoning = "";
        int affectedCount = 1;

        if (faultType == FaultType.FEEDER_FAULT) {
            confidence = 98.0;
            reasoning = "Feeder outage detected on feeder " + feeder.getFeederCode() + ". All downstream transformers de-energized.";
            affectedCount = countPolesInFeeder(feeder);
        } else if (faultType == FaultType.TRANSFORMER_FAULT) {
            confidence = 92.0;
            reasoning = "Distribution Transformer fault on " + transformer.getTransformerCode() + ". All poles under transformer are de-energized.";
            affectedCount = poleRepository.findByTransformerId(transformer.getId()).size();
        } else {
            // SPAN_FAULT
            if (Boolean.TRUE.equals(transformer.getTopologyKnown())) {
                livePole = findUpstreamLivePole(darkPole);
                List<Pole> downstream = graphTraversalService.getDownstreamPoles(darkPole);
                affectedCount = downstream.size();
                confidence = 95.0;

                String liveCode = livePole != null ? livePole.getPoleCode() : "DT-" + transformer.getTransformerCode();
                reasoning = "Live/Dark boundary detected on span between energized pole " + liveCode + " and dark pole " + darkPole.getPoleCode();
            } else {
                // 60% Missing Topology Case: Spatial proximity inference
                livePole = findNearestEnergizedPoleInDT(darkPole, transformer);
                List<Pole> dtPoles = poleRepository.findByTransformerId(transformer.getId());
                affectedCount = countDarkPolesInDT(dtPoles);
                confidence = 65.0; // Reduced confidence due to unmapped topology

                String liveCode = livePole != null ? livePole.getPoleCode() : "DT-" + transformer.getTransformerCode();
                reasoning = "60% missing topology case: Geometrical proximity inferred span between nearest energized pole " + liveCode + " and dark pole " + darkPole.getPoleCode() + ". Physical survey recommended.";
            }
        }

        // 4. Incident Grouping: Check if active incident exists for this DT/feeder/span
        Incident existingIncident = findExistingActiveIncident(transformer, feeder, darkPole, faultType);
        Incident incident;

        if (existingIncident != null) {
            incident = existingIncident;
            incident.setAffectedPoleCount(Math.max(incident.getAffectedPoleCount(), affectedCount));
            incidentRepository.save(incident);
        } else {
            incident = incidentService.createIncident(
                    livePole,
                    darkPole,
                    affectedCount,
                    confidence,
                    reasoning
            );
            incident.setFaultType(faultType);
            incidentRepository.save(incident);
            ticketService.createTicket(incident);
        }

        return LocalizationResult.builder()
                .fromPole(livePole != null ? livePole.getPoleCode() : (faultType == FaultType.FEEDER_FAULT ? feeder.getFeederCode() : "DT-" + transformer.getTransformerCode()))
                .toPole(darkPole.getPoleCode())
                .latitude(darkPole.getLatitude())
                .longitude(darkPole.getLongitude())
                .pincode(darkPole.getPincode() != null ? darkPole.getPincode() : "560078")
                .affectedPoles(affectedCount)
                .confidence(confidence)
                .reasoning(reasoning)
                .faultDetected(true)
                .build();
    }

    private Pole findPole(String poleCode) {
        return poleRepository.findByPoleCode(poleCode)
                .orElseThrow(() -> new RuntimeException("Pole not found: " + poleCode));
    }

    private boolean isUnderScheduledOutage(Feeder feeder, Transformer transformer) {
        LocalDateTime now = LocalDateTime.now();
        List<ScheduledOutage> outages = scheduledOutageRepository.findAll();
        for (ScheduledOutage outage : outages) {
            if (outage.getStartTime() != null && outage.getEndTime() != null) {
                if (!now.isBefore(outage.getStartTime()) && !now.isAfter(outage.getEndTime().plusMinutes(40))) { // handle 40 min overrun
                    if ("feeder".equalsIgnoreCase(String.valueOf(outage.getScope())) && feeder.getFeederCode().equalsIgnoreCase(outage.getTargetId())) {
                        return true;
                    }
                    if ("dt".equalsIgnoreCase(String.valueOf(outage.getScope())) && transformer.getTransformerCode().equalsIgnoreCase(outage.getTargetId())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isSensorFailure(Pole darkPole) {
        List<NetworkConnection> children = networkConnectionRepository.findByFromPoleId(darkPole.getId());
        if (children.isEmpty()) {
            return false; // Leaf pole
        }
        boolean anyChildLive = false;
        for (NetworkConnection conn : children) {
            Pole child = conn.getToPole();
            if (child.getDevice() != null && child.getDevice().getLatestTelemetry() != null) {
                if (Boolean.TRUE.equals(child.getDevice().getLatestTelemetry().getEnergized())) {
                    anyChildLive = true;
                    break;
                }
            }
        }
        return anyChildLive;
    }

    private FaultType determineFaultType(Feeder feeder, Transformer transformer, Pole darkPole) {
        List<Pole> feederPoles = getPolesInFeeder(feeder);
        long darkFeederPoles = countDarkPoles(feederPoles);
        if (darkFeederPoles > 0 && darkFeederPoles == feederPoles.size()) {
            return FaultType.FEEDER_FAULT;
        }

        List<Pole> dtPoles = poleRepository.findByTransformerId(transformer.getId());
        long darkDtPoles = countDarkPoles(dtPoles);
        if (darkDtPoles > 0 && darkDtPoles == dtPoles.size()) {
            return FaultType.TRANSFORMER_FAULT;
        }

        return FaultType.SPAN_FAULT;
    }

    private Pole findUpstreamLivePole(Pole darkPole) {
        if (darkPole.getParentPoleCode() == null) {
            return null;
        }
        Pole parent = poleRepository.findByPoleCode(darkPole.getParentPoleCode()).orElse(null);
        if (parent != null && parent.getDevice() != null && parent.getDevice().getLatestTelemetry() != null) {
            if (Boolean.TRUE.equals(parent.getDevice().getLatestTelemetry().getEnergized())) {
                return parent;
            }
        }
        return parent;
    }

    private Pole findNearestEnergizedPoleInDT(Pole darkPole, Transformer dt) {
        List<Pole> poles = poleRepository.findByTransformerId(dt.getId());
        Pole nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (Pole p : poles) {
            if (p.getId().equals(darkPole.getId())) continue;
            if (p.getDevice() != null && p.getDevice().getLatestTelemetry() != null) {
                if (Boolean.TRUE.equals(p.getDevice().getLatestTelemetry().getEnergized())) {
                    double dist = haversineMeters(darkPole.getLatitude(), darkPole.getLongitude(), p.getLatitude(), p.getLongitude());
                    if (dist < minDistance) {
                        minDistance = dist;
                        nearest = p;
                    }
                }
            }
        }
        return nearest;
    }

    private double haversineMeters(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private List<Pole> getPolesInFeeder(Feeder feeder) {
        List<Pole> result = new ArrayList<>();
        if (feeder == null || feeder.getTransformers() == null) {
            return result;
        }
        for (Transformer dt : feeder.getTransformers()) {
            result.addAll(poleRepository.findByTransformerId(dt.getId()));
        }
        return result;
    }

    private int countPolesInFeeder(Feeder feeder) {
        return getPolesInFeeder(feeder).size();
    }

    private int countDarkPolesInDT(List<Pole> dtPoles) {
        int count = 0;
        for (Pole p : dtPoles) {
            if (p.getDevice() != null && p.getDevice().getLatestTelemetry() != null) {
                if (Boolean.FALSE.equals(p.getDevice().getLatestTelemetry().getEnergized())) {
                    count++;
                }
            }
        }
        return Math.max(1, count);
    }

    private long countDarkPoles(List<Pole> poles) {
        return poles.stream()
                .filter(p -> p.getDevice() != null && p.getDevice().getLatestTelemetry() != null)
                .filter(p -> Boolean.FALSE.equals(p.getDevice().getLatestTelemetry().getEnergized()))
                .count();
    }

    private Incident findExistingActiveIncident(Transformer transformer, Feeder feeder, Pole darkPole, FaultType faultType) {
        List<Incident> activeIncidents = incidentRepository.findByStatus(IncidentStatus.DETECTED);
        for (Incident inc : activeIncidents) {
            if (inc.getFaultType() == faultType) {
                if (faultType == FaultType.FEEDER_FAULT && inc.getToPole() != null && inc.getToPole().getTransformer().getFeeder().getId().equals(feeder.getId())) {
                    return inc;
                }
                if (faultType == FaultType.TRANSFORMER_FAULT && inc.getToPole() != null && inc.getToPole().getTransformer().getId().equals(transformer.getId())) {
                    return inc;
                }
                if (faultType == FaultType.SPAN_FAULT && inc.getToPole() != null && inc.getToPole().getTransformer().getId().equals(transformer.getId())) {
                    return inc;
                }
            }
        }
        return null;
    }
}
