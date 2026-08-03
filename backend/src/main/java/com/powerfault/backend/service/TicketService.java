package com.powerfault.backend.service;

import com.powerfault.backend.entity.Incident;
import com.powerfault.backend.entity.Ticket;

public interface TicketService {

    Ticket createTicket(Incident incident);

    Ticket acknowledge(Long ticketId);

    Ticket assignCrew(Long ticketId, String crewName);

    Ticket resolve(Long ticketId);

    Ticket verify(Long ticketId);

    Ticket close(Long ticketId);

}