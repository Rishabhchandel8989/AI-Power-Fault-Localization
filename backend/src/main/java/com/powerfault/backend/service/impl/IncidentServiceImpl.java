

package com.powerfault.backend.service.impl;

import com.powerfault.backend.entity.Incident;
import com.powerfault.backend.entity.Pole;
import com.powerfault.backend.enums.FaultType;
import com.powerfault.backend.repository.IncidentRepository;
import com.powerfault.backend.service.IncidentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IncidentServiceImpl implements IncidentService {

    private final IncidentRepository incidentRepository;

    @Override
    public Incident createIncident(
            Pole livePole,
            Pole darkPole,
            int affectedPoleCount,
            double confidence,
            String reason
    ) {

        Incident incident = Incident.builder()
                .incidentNumber("INC-" + System.currentTimeMillis())
                .faultType(FaultType.SPAN_FAULT)
                .fromPole(livePole)
                .toPole(darkPole)
                .latitude(darkPole.getLatitude())
                .longitude(darkPole.getLongitude())
                .pincode(
                        darkPole.getPincode() != null
                                ? darkPole.getPincode()
                                : "UNKNOWN"
                )
                .affectedPoleCount(affectedPoleCount)
                .confidence(confidence)
                .reasoning(reason)
                .build();

        return incidentRepository.save(incident);
    }
}