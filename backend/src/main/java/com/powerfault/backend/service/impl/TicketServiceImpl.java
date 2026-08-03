package com.powerfault.backend.service.impl;

import com.powerfault.backend.entity.Incident;
import com.powerfault.backend.entity.Ticket;
import com.powerfault.backend.enums.TicketStatus;
import com.powerfault.backend.repository.TicketRepository;
import com.powerfault.backend.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;

    @Override
    public Ticket createTicket(Incident incident) {

        Ticket ticket = Ticket.builder()
                .ticketNumber("TKT-" + System.currentTimeMillis())
                .incident(incident)
                .status(TicketStatus.DETECTED)
                .build();

        return ticketRepository.save(ticket);
    }

    @Override
    public Ticket acknowledge(Long ticketId) {
        return updateStatus(ticketId, TicketStatus.ACKNOWLEDGED);
    }

    @Override
    public Ticket assignCrew(Long ticketId, String crewName) {
        return updateStatus(ticketId, TicketStatus.CREW_ASSIGNED);
    }

    @Override
    public Ticket resolve(Long ticketId) {
        return updateStatus(ticketId, TicketStatus.RESOLVED);
    }

    @Override
    public Ticket verify(Long ticketId) {
        return updateStatus(ticketId, TicketStatus.VERIFIED);
    }

    @Override
    public Ticket close(Long ticketId) {
        return updateStatus(ticketId, TicketStatus.CLOSED);
    }

    private Ticket updateStatus(Long ticketId,
                                TicketStatus status) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new RuntimeException("Ticket not found"));

        ticket.setStatus(status);

        return ticketRepository.save(ticket);
    }

}