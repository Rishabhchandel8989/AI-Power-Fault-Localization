package com.powerfault.backend.simulator.impl;

import com.powerfault.backend.dto.request.TelemetryRequest;
import com.powerfault.backend.dto.response.LocalizationResult;
import com.powerfault.backend.entity.*;
import com.powerfault.backend.enums.OutageScope;
import com.powerfault.backend.repository.*;
import com.powerfault.backend.service.LocalizationService;
import com.powerfault.backend.service.graph.GraphTraversalService;
import com.powerfault.backend.simulator.SimulatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SimulatorServiceImpl implements SimulatorService {

    private final PoleRepository poleRepository;
    private final DeviceRepository deviceRepository;
    private final DeviceTelemetryRepository deviceTelemetryRepository;
    private final TransformerRepository transformerRepository;
    private final FeederRepository feederRepository;
    private final IncidentRepository incidentRepository;
    private final TicketRepository ticketRepository;
    private final ScheduledOutageRepository scheduledOutageRepository;
    private final GraphTraversalService graphTraversalService;
    private final LocalizationService localizationService;

    private final Random random = new Random();

    @Override
    @Transactional
    public Map<String, Object> simulateSpanFault(String poleCode) {
        Pole targetPole = poleRepository.findByPoleCode(poleCode)
                .orElseThrow(() -> new RuntimeException("Pole not found: " + poleCode));

        List<Pole> affectedPoles;
        if (Boolean.TRUE.equals(targetPole.getTransformer().getTopologyKnown())) {
            affectedPoles = graphTraversalService.getDownstreamPoles(targetPole);
        } else {
            // Unmapped topology: affect target pole and subsequent poles under same DT
            List<Pole> dtPoles = poleRepository.findByTransformerId(targetPole.getTransformer().getId());
            affectedPoles = dtPoles.stream().filter(p -> p.getId() >= targetPole.getId()).toList();
        }

        LocalizationResult result = null;
        int countPoles = 0;

        for (Pole pole : affectedPoles) {
            countPoles++;
            if (pole.getDevice() != null) {
                Device device = pole.getDevice();
                // Simulate firmware 1.2 quiet behavior vs FW 1.4 capacitor dying packet (~70% success)
                if ("1.2.0".equals(device.getFirmwareVersion())) {
                    // Quiet device: update state without sending power_lost event
                    updateDeviceState(device, false, "quiet");
                } else {
                    // FW 1.4+: 70% send power_lost, 30% capacitor exhausted
                    boolean msgArrived = random.nextDouble() <= 0.70 || pole.getPoleCode().equalsIgnoreCase(poleCode);
                    if (msgArrived) {
                        TelemetryRequest req = TelemetryRequest.builder()
                                .deviceId(device.getDeviceId())
                                .poleId(pole.getPoleCode())
                                .event("power_lost")
                                .energized(false)
                                .sequenceNumber(getNextSeq(device))
                                .batteryMv(3100)
                                .rssi(-88)
                                .timestamp(LocalDateTime.now())
                                .build();
                        result = localizationService.localizeFault(req);
                    } else {
                        updateDeviceState(device, false, "power_lost_lost");
                    }
                }
            }
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("status", "SUCCESS");
        resp.put("message", "Span fault injected at pole " + poleCode + ". " + countPoles + " poles de-energized.");
        resp.put("localization", result);
        return resp;
    }

    @Override
    @Transactional
    public Map<String, Object> simulateTransformerFault(String transformerCode) {
        Transformer dt = transformerRepository.findByTransformerCode(transformerCode)
                .orElseThrow(() -> new RuntimeException("Transformer not found: " + transformerCode));

        List<Pole> poles = poleRepository.findByTransformerId(dt.getId());
        LocalizationResult result = null;

        for (Pole pole : poles) {
            if (pole.getDevice() != null) {
                Device device = pole.getDevice();
                TelemetryRequest req = TelemetryRequest.builder()
                        .deviceId(device.getDeviceId())
                        .poleId(pole.getPoleCode())
                        .event("power_lost")
                        .energized(false)
                        .sequenceNumber(getNextSeq(device))
                        .batteryMv(3200)
                        .rssi(-85)
                        .timestamp(LocalDateTime.now())
                        .build();
                result = localizationService.localizeFault(req);
            }
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("status", "SUCCESS");
        resp.put("message", "Distribution Transformer fault injected for " + transformerCode + ". All poles dark.");
        resp.put("localization", result);
        return resp;
    }

    @Override
    @Transactional
    public Map<String, Object> simulateFeederFault(String feederCode) {
        Feeder feeder = feederRepository.findByFeederCode(feederCode)
                .orElseThrow(() -> new RuntimeException("Feeder not found: " + feederCode));

        LocalizationResult result = null;
        int poleCount = 0;

        for (Transformer dt : feeder.getTransformers()) {
            List<Pole> poles = poleRepository.findByTransformerId(dt.getId());
            for (Pole pole : poles) {
                poleCount++;
                if (pole.getDevice() != null) {
                    Device device = pole.getDevice();
                    TelemetryRequest req = TelemetryRequest.builder()
                            .deviceId(device.getDeviceId())
                            .poleId(pole.getPoleCode())
                            .event("power_lost")
                            .energized(false)
                            .sequenceNumber(getNextSeq(device))
                            .batteryMv(3150)
                            .rssi(-90)
                            .timestamp(LocalDateTime.now())
                            .build();
                    result = localizationService.localizeFault(req);
                }
            }
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("status", "SUCCESS");
        resp.put("message", "Feeder fault injected for " + feederCode + ". " + poleCount + " poles de-energized.");
        resp.put("localization", result);
        return resp;
    }

    @Override
    @Transactional
    public Map<String, Object> simulateDeviceFailure(String poleCode) {
        Pole pole = poleRepository.findByPoleCode(poleCode)
                .orElseThrow(() -> new RuntimeException("Pole not found: " + poleCode));

        if (pole.getDevice() == null) {
            throw new RuntimeException("Pole " + poleCode + " does not have an IoT device fitted.");
        }

        Device device = pole.getDevice();
        TelemetryRequest req = TelemetryRequest.builder()
                .deviceId(device.getDeviceId())
                .poleId(pole.getPoleCode())
                .event("power_lost")
                .energized(false)
                .sequenceNumber(getNextSeq(device))
                .batteryMv(2900)
                .rssi(-105)
                .timestamp(LocalDateTime.now())
                .build();

        LocalizationResult result = localizationService.localizeFault(req);

        Map<String, Object> resp = new HashMap<>();
        resp.put("status", "SUCCESS");
        resp.put("message", "Sensor / Modem failure simulated for device " + device.getDeviceId() + " on pole " + poleCode + ". Children remain live.");
        resp.put("localization", result);
        return resp;
    }

    @Override
    @Transactional
    public Map<String, Object> simulateScheduledOutage(String scope, String targetId, String reason) {
        ScheduledOutage outage = ScheduledOutage.builder()
                .outageId("SO-" + System.currentTimeMillis() % 10000)
                .scope("feeder".equalsIgnoreCase(scope) ? OutageScope.FEEDER : OutageScope.TRANSFORMER)
                .targetId(targetId)
                .startTime(LocalDateTime.now().minusMinutes(5))
                .endTime(LocalDateTime.now().plusHours(2))
                .reason(reason != null ? reason : "Planned substation feeder maintenance")
                .build();

        scheduledOutageRepository.save(outage);

        Map<String, Object> resp = new HashMap<>();
        resp.put("status", "SUCCESS");
        resp.put("message", "Scheduled outage registered for " + scope + " " + targetId + " (Valid for 2 hours).");
        return resp;
    }

    @Override
    @Transactional
    public Map<String, Object> repairFault(String poleCode) {
        Pole pole = poleRepository.findByPoleCode(poleCode)
                .orElseThrow(() -> new RuntimeException("Pole not found: " + poleCode));

        Transformer dt = pole.getTransformer();
        List<Pole> dtPoles = poleRepository.findByTransformerId(dt.getId());

        int restoredCount = 0;
        for (Pole p : dtPoles) {
            if (p.getDevice() != null) {
                Device device = p.getDevice();
                TelemetryRequest req = TelemetryRequest.builder()
                        .deviceId(device.getDeviceId())
                        .poleId(p.getPoleCode())
                        .event("power_restored")
                        .energized(true)
                        .sequenceNumber(getNextSeq(device))
                        .batteryMv(3600)
                        .rssi(-72)
                        .timestamp(LocalDateTime.now())
                        .build();

                localizationService.localizeFault(req);
                restoredCount++;
            }
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("status", "SUCCESS");
        resp.put("message", "Power restored across transformer " + dt.getTransformerCode() + ". " + restoredCount + " pole sensors reported power_restored telemetry. Active tickets auto-verified.");
        return resp;
    }

    @Override
    @Transactional
    public Map<String, Object> resetNetwork() {
        List<Device> devices = deviceRepository.findAll();
        for (Device device : devices) {
            DeviceTelemetry telem = deviceTelemetryRepository.findByDevice(device).orElse(null);
            if (telem != null) {
                telem.setEnergized(true);
                telem.setEvent("boot");
                telem.setTimestamp(LocalDateTime.now());
                deviceTelemetryRepository.save(telem);
            }
        }

        ticketRepository.deleteAll();
        incidentRepository.deleteAll();

        Map<String, Object> resp = new HashMap<>();
        resp.put("status", "SUCCESS");
        resp.put("message", "Network state reset. All sensors energized (true), active tickets cleared.");
        return resp;
    }

    private void updateDeviceState(Device device, boolean energized, String event) {
        DeviceTelemetry telem = deviceTelemetryRepository.findByDevice(device).orElse(null);
        if (telem != null) {
            telem.setEnergized(energized);
            telem.setEvent(event);
            telem.setTimestamp(LocalDateTime.now());
            deviceTelemetryRepository.save(telem);
        }
    }

    private int getNextSeq(Device device) {
        DeviceTelemetry telem = deviceTelemetryRepository.findByDevice(device).orElse(null);
        if (telem == null || telem.getSequenceNumber() == null) return 1;
        return telem.getSequenceNumber() + 1;
    }
}