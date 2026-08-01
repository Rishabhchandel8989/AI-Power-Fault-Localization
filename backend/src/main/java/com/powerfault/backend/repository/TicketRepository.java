package com.powerfault.backend.repository;

import com.powerfault.backend.entity.Ticket;
import com.powerfault.backend.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository
        extends JpaRepository<Ticket, Long> {

    List<Ticket> findByStatus(TicketStatus status);

}