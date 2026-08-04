package com.powerfault.backend.controller;

import com.powerfault.backend.entity.Pole;
import com.powerfault.backend.entity.Ticket;
import com.powerfault.backend.enums.IncidentStatus;
import com.powerfault.backend.enums.TicketStatus;
import com.powerfault.backend.repository.DeviceRepository;
import com.powerfault.backend.repository.IncidentRepository;
import com.powerfault.backend.repository.PoleRepository;
import com.powerfault.backend.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final PoleRepository poleRepository;
    private final DeviceRepository deviceRepository;
    private final IncidentRepository incidentRepository;
    private final TicketRepository ticketRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getDashboardMetrics() {
        long totalPoles = poleRepository.count();
        long totalDevices = deviceRepository.count();
        
        List<Pole> poles = poleRepository.findAll();
        long darkPoles = poles.stream()
                .filter(p -> p.getDevice() != null && p.getDevice().getLatestTelemetry() != null)
                .filter(p -> Boolean.FALSE.equals(p.getDevice().getLatestTelemetry().getEnergized()))
                .count();

        long activeIncidents = incidentRepository.findByStatus(IncidentStatus.DETECTED).size();
        long activeTickets = ticketRepository.findAll().stream()
                .filter(t -> t.getStatus() != TicketStatus.CLOSED)
                .count();
        long closedTickets = ticketRepository.findByStatus(TicketStatus.CLOSED).size();

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalPoles", totalPoles);
        metrics.put("totalDevices", totalDevices);
        metrics.put("darkPoles", darkPoles);
        metrics.put("activeIncidents", activeIncidents);
        metrics.put("activeTickets", activeTickets);
        metrics.put("closedTickets", closedTickets);

        return ResponseEntity.ok(metrics);
    }
}
