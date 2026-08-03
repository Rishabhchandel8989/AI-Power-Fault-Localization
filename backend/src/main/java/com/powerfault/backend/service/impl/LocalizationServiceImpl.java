package com.powerfault.backend.service.impl;

import com.powerfault.backend.entity.Incident;
import com.powerfault.backend.service.TicketService;
import com.powerfault.backend.service.graph.GraphTraversalService;

import com.powerfault.backend.dto.request.TelemetryRequest;
import com.powerfault.backend.dto.response.LocalizationResult;
import com.powerfault.backend.entity.Pole;
import com.powerfault.backend.repository.*;
import com.powerfault.backend.service.IncidentService;
import com.powerfault.backend.service.LocalizationService;
import com.powerfault.backend.service.TelemetryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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

    private final TelemetryService telemetryService;

    @Override
    public LocalizationResult localizeFault(TelemetryRequest request) {

        telemetryService.processTelemetry(request);

        Pole darkPole = findPole(request.getPoleId());

        Pole livePole = findLastLivePole(darkPole);

//        int List<Pole> affectedPoles =
//                graphTraversalService.getDownstreamPoles(darkPole);
//
//        int affected = affectedPoles.size();
        int affected = countAffectedPoles(darkPole);

        double confidence = calculateConfidence(darkPole);

        String reason = buildReason(livePole, darkPole);

        Incident incident = incidentService.createIncident(
                livePole,
                darkPole,
                affected,
                confidence,
                reason
        );

        ticketService.createTicket(incident);

        return LocalizationResult.builder()
                .fromPole(livePole != null ? livePole.getPoleCode() : "UNKNOWN")
                .toPole(darkPole.getPoleCode())
                .latitude(darkPole.getLatitude())
                .longitude(darkPole.getLongitude())
                .pincode(darkPole.getPincode())
                .affectedPoles(affected)
                .confidence(confidence)
                .reasoning(reason)
                .build();
    }

    private Pole findPole(String poleCode) {

        return poleRepository
                .findByPoleCode(poleCode)
                .orElseThrow(() ->
                        new RuntimeException("Pole not found: " + poleCode));
    }

    private Pole findLastLivePole(Pole darkPole) {

        if (darkPole.getParentPoleCode() == null) {
            return null;
        }

        return poleRepository
                .findByPoleCode(darkPole.getParentPoleCode())
                .orElse(null);
    }

    private int countAffectedPoles(Pole pole) {

        return networkConnectionRepository
                .findByFromPoleId(pole.getId())
                .size() + 1;
    }

    private double calculateConfidence(Pole pole) {

        if (pole.getParentPoleCode() == null) {
            return 60.0;
        }

        if (Boolean.FALSE.equals(pole.getHasDevice())) {
            return 75.0;
        }

        return 95.0;
    }
    private String buildReason(Pole livePole, Pole darkPole) {

        if (livePole == null) {

            return "Topology unavailable. Estimated outage near pole "
                    + darkPole.getPoleCode()
                    + ". Confidence reduced.";

        }

        return "Boundary detected between energized pole "
                + livePole.getPoleCode()
                + " and de-energized pole "
                + darkPole.getPoleCode();
    }

    private final GraphTraversalService graphTraversalService;
}
