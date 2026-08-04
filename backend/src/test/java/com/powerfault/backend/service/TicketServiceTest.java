package com.powerfault.backend.service;

import com.powerfault.backend.entity.*;
import com.powerfault.backend.enums.IncidentStatus;
import com.powerfault.backend.enums.TicketStatus;
import com.powerfault.backend.repository.IncidentRepository;
import com.powerfault.backend.repository.PoleRepository;
import com.powerfault.backend.repository.TicketRepository;
import com.powerfault.backend.service.impl.TicketServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private IncidentRepository incidentRepository;
    @Mock
    private PoleRepository poleRepository;

    @InjectMocks
    private TicketServiceImpl ticketService;

    private Incident incident;
    private Ticket ticket;
    private Pole darkPole;

    @BeforeEach
    void setUp() {
        Transformer dt = Transformer.builder().id(1L).transformerCode("D-0111").build();
        darkPole = Pole.builder().id(101L).poleCode("P-0111-01").transformer(dt).build();
        Device dev = Device.builder().id(201L).pole(darkPole).build();
        DeviceTelemetry telem = DeviceTelemetry.builder().id(301L).energized(false).device(dev).build();
        dev.setLatestTelemetry(telem);
        darkPole.setDevice(dev);

        incident = Incident.builder()
                .id(1L)
                .incidentNumber("INC-100")
                .toPole(darkPole)
                .status(IncidentStatus.DETECTED)
                .build();

        ticket = Ticket.builder()
                .id(1L)
                .ticketNumber("TKT-100")
                .incident(incident)
                .status(TicketStatus.DETECTED)
                .build();
    }

    @Test
    void testPrematureResolutionRejectionWhenPolesDark() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(poleRepository.findByTransformerId(1L)).thenReturn(List.of(darkPole));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            ticketService.updateTicketStatus(1L, TicketStatus.RESOLVED);
        });

        assertTrue(ex.getMessage().contains("Restoration unverified"));
    }
}
