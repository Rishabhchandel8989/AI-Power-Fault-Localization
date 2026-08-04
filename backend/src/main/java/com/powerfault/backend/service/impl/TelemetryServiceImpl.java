package com.powerfault.backend.service.impl;

import com.powerfault.backend.dto.request.TelemetryRequest;
import com.powerfault.backend.entity.Device;
import com.powerfault.backend.entity.DeviceTelemetry;
import com.powerfault.backend.entity.Pole;
import com.powerfault.backend.entity.TelemetryHistory;
import com.powerfault.backend.enums.DeviceStatus;
import com.powerfault.backend.repository.DeviceRepository;
import com.powerfault.backend.repository.DeviceTelemetryRepository;
import com.powerfault.backend.repository.PoleRepository;
import com.powerfault.backend.repository.TelemetryHistoryRepository;
import com.powerfault.backend.service.LocalizationService;
import com.powerfault.backend.service.TelemetryService;
import com.powerfault.backend.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TelemetryServiceImpl implements TelemetryService {

    private final DeviceRepository deviceRepository;
    private final DeviceTelemetryRepository deviceTelemetryRepository;
    private final TelemetryHistoryRepository telemetryHistoryRepository;
    private final PoleRepository poleRepository;

    @Lazy
    private final LocalizationService localizationService;
    @Lazy
    private final TicketService ticketService;

    @Override
    public DeviceTelemetry processTelemetry(TelemetryRequest request) {

        Device device = deviceRepository.findByDeviceId(request.getDeviceId())
                .orElseGet(() -> {
                    if (request.getPoleId() != null) {
                        Pole pole = poleRepository.findByPoleCode(request.getPoleId()).orElse(null);
                        if (pole != null) {
                            Device d = Device.builder()
                                    .deviceId(request.getDeviceId())
                                    .firmwareVersion("1.4.2")
                                    .lastSeen(LocalDateTime.now())
                                    .status(DeviceStatus.ONLINE)
                                    .pole(pole)
                                    .build();
                            return deviceRepository.save(d);
                        }
                    }
                    throw new RuntimeException("Device not found: " + request.getDeviceId());
                });

        DeviceTelemetry latest = deviceTelemetryRepository.findByDevice(device).orElse(null);

        // Sequence deduplication (allow boot event to reset sequence)
        if (latest != null && !"boot".equalsIgnoreCase(request.getEvent())) {
            if (request.getSequenceNumber() != null && latest.getSequenceNumber() != null
                    && request.getSequenceNumber() <= latest.getSequenceNumber()) {
                return latest; // Ignore duplicate/out-of-order packet
            }
        }

        if (latest == null) {
            latest = new DeviceTelemetry();
            latest.setDevice(device);
        }

        latest.setEnergized(request.getEnergized());
        latest.setEvent(request.getEvent());
        latest.setSequenceNumber(request.getSequenceNumber());
        latest.setBatteryMv(request.getBatteryMv());
        latest.setRssi(request.getRssi());
        latest.setTimestamp(request.getTimestamp() != null ? request.getTimestamp() : LocalDateTime.now());

        deviceTelemetryRepository.save(latest);

        device.setLastSeen(LocalDateTime.now());
        device.setStatus(request.getEnergized() ? DeviceStatus.ONLINE : DeviceStatus.OFFLINE);
        deviceRepository.save(device);

        TelemetryHistory history = TelemetryHistory.builder()
                .device(device)
                .energized(request.getEnergized())
                .event(request.getEvent())
                .sequenceNumber(request.getSequenceNumber())
                .batteryMv(request.getBatteryMv())
                .rssi(request.getRssi())
                .timestamp(request.getTimestamp() != null ? request.getTimestamp() : LocalDateTime.now())
                .build();
        telemetryHistoryRepository.save(history);

        // Check if restoration occurred
        if (Boolean.TRUE.equals(request.getEnergized())) {
            ticketService.checkAutoVerification(device.getPole());
        }

        return latest;
    }
}