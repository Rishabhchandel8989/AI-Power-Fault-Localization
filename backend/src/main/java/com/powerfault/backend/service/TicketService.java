package com.powerfault.backend.service;

import com.powerfault.backend.entity.Incident;
import com.powerfault.backend.entity.Pole;
import com.powerfault.backend.entity.Ticket;
import com.powerfault.backend.enums.TicketStatus;

import java.util.List;

public interface TicketService {

    Ticket createTicket(Incident incident);

    Ticket acknowledge(Long ticketId);

    Ticket assignCrew(Long ticketId, String crewName);

    Ticket resolve(Long ticketId);

    Ticket verify(Long ticketId);

    Ticket close(Long ticketId);

    List<Ticket> getAllTickets();

    Ticket getTicketById(Long ticketId);

    Ticket updateTicketStatus(Long ticketId, TicketStatus newStatus);

    void checkAutoVerification(Pole pole);
}