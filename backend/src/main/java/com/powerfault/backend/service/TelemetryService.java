//package com.powerfault.backend.service;
//
//import com.powerfault.backend.dto.request.TelemetryRequest;
//import com.powerfault.backend.entity.Device;
//import com.powerfault.backend.entity.DeviceTelemetry;
//import com.powerfault.backend.entity.TelemetryHistory;
//import com.powerfault.backend.repository.DeviceRepository;
//import com.powerfault.backend.repository.DeviceTelemetryRepository;
//import com.powerfault.backend.repository.TelemetryHistoryRepository;
//
//public interface TelemetryService {
//
//    private final DeviceRepository deviceRepository;
//
//    private final DeviceTelemetryRepository deviceTelemetryRepository;
//
//    private final TelemetryHistoryRepository telemetryHistoryRepository;
//
//    @Override
//    public DeviceTelemetry processTelemetry(TelemetryRequest request) {
//
//        Device device = deviceRepository
//                .findByDeviceId(request.getDeviceId())
//                .orElseThrow(() ->
//                        new RuntimeException("Device not found: "
//                                + request.getDeviceId()));
//
//        DeviceTelemetry latest =
//                deviceTelemetryRepository
//                        .findByDevice(device)
//                        .orElse(null);
//
//        if (latest != null &&
//                request.getSequenceNumber() <= latest.getSequenceNumber()) {
//
//            return latest;
//        }
//
//        if (latest == null) {
//
//            latest = new DeviceTelemetry();
//
//            latest.setDevice(device);
//        }
//
//        latest.setEnergized(request.getEnergized());
//        latest.setEvent(request.getEvent());
//        latest.setSequenceNumber(request.getSequenceNumber());
//        latest.setBatteryMv(request.getBatteryMv());
//        latest.setRssi(request.getRssi());
//        latest.setTimestamp(request.getTimestamp());
//        deviceTelemetryRepository.save(latest);
//
//        TelemetryHistory history = TelemetryHistory.builder()
//                .device(device)
//                .energized(request.getEnergized())
//                .event(request.getEvent())
//                .sequenceNumber(request.getSequenceNumber())
//                .batteryMv(request.getBatteryMv())
//                .rssi(request.getRssi())
//                .timestamp(request.getTimestamp())
//                .build();
//
//        telemetryHistoryRepository.save(history);
//
//
//        return latest;
//    }
//
//
//}



package com.powerfault.backend.service;

import com.powerfault.backend.dto.request.TelemetryRequest;
import com.powerfault.backend.entity.DeviceTelemetry;

public interface TelemetryService {

    DeviceTelemetry processTelemetry(TelemetryRequest request);

//    DeviceTelemetry latest =
//            deviceTelemetryRepository
//                    .findByDevice(device)
//                    .orElse(null);

}