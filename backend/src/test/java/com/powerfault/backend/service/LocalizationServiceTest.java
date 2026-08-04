package com.powerfault.backend.service;

import com.powerfault.backend.dto.request.TelemetryRequest;
import com.powerfault.backend.dto.response.LocalizationResult;
import com.powerfault.backend.entity.*;
import com.powerfault.backend.enums.FaultType;
import com.powerfault.backend.enums.IncidentStatus;
import com.powerfault.backend.repository.*;
import com.powerfault.backend.service.graph.GraphTraversalService;
import com.powerfault.backend.service.impl.LocalizationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LocalizationServiceTest {

    @Mock
    private PoleRepository poleRepository;
    @Mock
    private IncidentService incidentService;
    @Mock
    private TicketService ticketService;
    @Mock
    private DeviceRepository deviceRepository;
    @Mock
    private DeviceTelemetryRepository deviceTelemetryRepository;
    @Mock
    private IncidentRepository incidentRepository;
    @Mock
    private NetworkConnectionRepository networkConnectionRepository;
    @Mock
    private ScheduledOutageRepository scheduledOutageRepository;
    @Mock
    private TelemetryService telemetryService;
    @Mock
    private GraphTraversalService graphTraversalService;

    @InjectMocks
    private LocalizationServiceImpl localizationService;

    private Transformer knownDt;
    private Transformer unknownDt;
    private Feeder feeder;
    private Pole livePole;
    private Pole darkPole;

    @BeforeEach
    void setUp() {
        feeder = Feeder.builder().id(1L).feederCode("F-07-01").build();
        knownDt = Transformer.builder().id(10L).transformerCode("D-0111").topologyKnown(true).feeder(feeder).build();
        unknownDt = Transformer.builder().id(11L).transformerCode("D-0113").topologyKnown(false).feeder(feeder).build();
        feeder.setTransformers(List.of(knownDt, unknownDt));

        livePole = Pole.builder()
                .id(101L)
                .poleCode("P-0111-01")
                .latitude(12.9682)
                .longitude(77.5946)
                .sequenceNumber(1)
                .transformer(knownDt)
                .build();

        darkPole = Pole.builder()
                .id(102L)
                .poleCode("P-0111-02")
                .latitude(12.9686)
                .longitude(77.5949)
                .sequenceNumber(2)
                .parentPoleCode("P-0111-01")
                .transformer(knownDt)
                .build();
    }

    @Test
    void testSpanFaultLocalizationKnownTopology() {
        TelemetryRequest req = TelemetryRequest.builder()
                .poleId("P-0111-02")
                .deviceId("DEV-102")
                .event("power_lost")
                .energized(false)
                .sequenceNumber(2)
                .timestamp(LocalDateTime.now())
                .build();

        when(poleRepository.findByPoleCode("P-0111-02")).thenReturn(Optional.of(darkPole));
        when(poleRepository.findByPoleCode("P-0111-01")).thenReturn(Optional.of(livePole));
        when(scheduledOutageRepository.findAll()).thenReturn(Collections.emptyList());
        when(networkConnectionRepository.findByFromPoleId(102L)).thenReturn(Collections.emptyList());
        when(poleRepository.findByTransformerId(10L)).thenReturn(List.of(livePole, darkPole));
        when(graphTraversalService.getDownstreamPoles(darkPole)).thenReturn(List.of(darkPole));

        Incident mockIncident = Incident.builder()
                .id(1L)
                .incidentNumber("INC-001")
                .faultType(FaultType.SPAN_FAULT)
                .fromPole(livePole)
                .toPole(darkPole)
                .affectedPoleCount(1)
                .confidence(95.0)
                .build();

        when(incidentService.createIncident(any(), any(), anyInt(), anyDouble(), anyString())).thenReturn(mockIncident);

        LocalizationResult result = localizationService.localizeFault(req);

        assertTrue(result.isFaultDetected());
        assertEquals("P-0111-01", result.getFromPole());
        assertEquals("P-0111-02", result.getToPole());
        assertEquals(95.0, result.getConfidence());
        verify(ticketService, times(1)).createTicket(any());
    }

    @Test
    void testMissingTopologyFallbackConfidence() {
        darkPole.setTransformer(unknownDt);
        darkPole.setParentPoleCode(null);

        TelemetryRequest req = TelemetryRequest.builder()
                .poleId("P-0111-02")
                .deviceId("DEV-102")
                .event("power_lost")
                .energized(false)
                .sequenceNumber(2)
                .timestamp(LocalDateTime.now())
                .build();

        when(poleRepository.findByPoleCode("P-0111-02")).thenReturn(Optional.of(darkPole));
        when(scheduledOutageRepository.findAll()).thenReturn(Collections.emptyList());
        when(networkConnectionRepository.findByFromPoleId(102L)).thenReturn(Collections.emptyList());
        when(poleRepository.findByTransformerId(11L)).thenReturn(List.of(livePole, darkPole));

        Incident mockIncident = Incident.builder()
                .id(2L)
                .incidentNumber("INC-002")
                .faultType(FaultType.SPAN_FAULT)
                .affectedPoleCount(1)
                .confidence(65.0)
                .build();

        when(incidentService.createIncident(any(), any(), anyInt(), anyDouble(), anyString())).thenReturn(mockIncident);

        LocalizationResult result = localizationService.localizeFault(req);

        assertTrue(result.isFaultDetected());
        assertEquals(65.0, result.getConfidence());
    }
}
