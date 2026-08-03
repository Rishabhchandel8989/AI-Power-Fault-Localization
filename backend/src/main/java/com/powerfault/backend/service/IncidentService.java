package com.powerfault.backend.service;

import com.powerfault.backend.entity.Incident;
import com.powerfault.backend.entity.Pole;

public interface IncidentService {

    Incident createIncident(
            Pole livePole,
            Pole darkPole,
            int affectedPoleCount,
            double confidence,
            String reason
    );

}