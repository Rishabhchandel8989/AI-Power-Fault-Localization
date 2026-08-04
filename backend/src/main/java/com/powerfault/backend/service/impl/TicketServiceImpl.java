package com.powerfault.backend.service.impl;

import com.powerfault.backend.entity.DeviceTelemetry;
import com.powerfault.backend.entity.Incident;
import com.powerfault.backend.entity.Pole;
import com.powerfault.backend.entity.Ticket;
import com.powerfault.backend.enums.IncidentStatus;
import com.powerfault.backend.enums.TicketStatus;
import com.powerfault.backend.repository.IncidentRepository;
import com.powerfault.backend.repository.PoleRepository;
import com.powerfault.backend.repository.TicketRepository;
import com.powerfault.backend.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final IncidentRepository incidentRepository;
    private final PoleRepository poleRepository;

    @Override
    public Ticket createTicket(Incident incident) {
        Ticket ticket = Ticket.builder()
                .ticketNumber("TKT-" + String.format("%06d", System.currentTimeMillis() % 1000000))
                .incident(incident)
                .status(TicketStatus.DETECTED)
                .build();

        return ticketRepository.save(ticket);
    }

    @Override
    public Ticket acknowledge(Long ticketId) {
        return updateTicketStatus(ticketId, TicketStatus.ACKNOWLEDGED);
    }

    @Override
    public Ticket assignCrew(Long ticketId, String crewName) {
        return updateTicketStatus(ticketId, TicketStatus.CREW_ASSIGNED);
    }

    @Override
    public Ticket resolve(Long ticketId) {
        return updateTicketStatus(ticketId, TicketStatus.RESOLVED);
    }

    @Override
    public Ticket verify(Long ticketId) {
        return updateTicketStatus(ticketId, TicketStatus.VERIFIED);
    }

    @Override
    public Ticket close(Long ticketId) {
        return updateTicketStatus(ticketId, TicketStatus.CLOSED);
    }

    @Override
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    @Override
    public Ticket getTicketById(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found: " + ticketId));
    }

    @Override
    @Transactional
    public Ticket updateTicketStatus(Long ticketId, TicketStatus newStatus) {
        Ticket ticket = getTicketById(ticketId);
        Incident incident = ticket.getIncident();

        // Premature Resolution Protection:
        // If operator attempts to set status to RESOLVED, VERIFIED, or CLOSED manually,
        // verify from telemetry first!
        if (newStatus == TicketStatus.RESOLVED || newStatus == TicketStatus.VERIFIED || newStatus == TicketStatus.CLOSED) {
            boolean allRestored = isIncidentRestoredFromTelemetry(incident);
            if (!allRestored) {
                throw new IllegalStateException(
                        "Restoration unverified! Telemetry from field sensors indicates affected poles remain dark. Ticket resolution rejected until power restoration is confirmed by IoT telemetry."
                );
            }
        }

        ticket.setStatus(newStatus);
        if (newStatus == TicketStatus.RESOLVED) {
            ticket.setResolvedAt(LocalDateTime.now());
            incident.setStatus(IncidentStatus.VERIFIED);
            incident.setVerifiedAt(LocalDateTime.now());
        } else if (newStatus == TicketStatus.CLOSED) {
            incident.setStatus(IncidentStatus.CLOSED);
            incident.setClosedAt(LocalDateTime.now());
        }

        incidentRepository.save(incident);
        return ticketRepository.save(ticket);
    }

    @Override
    @Transactional
    public void checkAutoVerification(Pole pole) {
        List<Ticket> activeTickets = ticketRepository.findAll().stream()
                .filter(t -> t.getStatus() != TicketStatus.CLOSED && t.getStatus() != TicketStatus.VERIFIED)
                .toList();

        for (Ticket ticket : activeTickets) {
            Incident incident = ticket.getIncident();
            if (isIncidentRestoredFromTelemetry(incident)) {
                // Auto-verify and close ticket!
                ticket.setStatus(TicketStatus.VERIFIED);
                ticket.setResolvedAt(LocalDateTime.now());
                ticketRepository.save(ticket);

                ticket.setStatus(TicketStatus.CLOSED);
                ticketRepository.save(ticket);

                incident.setStatus(IncidentStatus.CLOSED);
                incident.setClosedAt(LocalDateTime.now());
                incident.setVerifiedAt(LocalDateTime.now());
                incidentRepository.save(incident);

                System.out.println("✓ Auto-Verified Ticket " + ticket.getTicketNumber() + " from IoT telemetry! Power restored.");
            }
        }
    }

    private boolean isIncidentRestoredFromTelemetry(Incident incident) {
        if (incident.getToPole() == null) return true;
        Pole darkPole = incident.getToPole();
        List<Pole> dtPoles = poleRepository.findByTransformerId(darkPole.getTransformer().getId());

        for (Pole p : dtPoles) {
            if (p.getDevice() != null && p.getDevice().getLatestTelemetry() != null) {
                DeviceTelemetry telem = p.getDevice().getLatestTelemetry();
                if (Boolean.FALSE.equals(telem.getEnergized())) {
                    return false; // Still de-energized
                }
            }
        }
        return true;
    }
}