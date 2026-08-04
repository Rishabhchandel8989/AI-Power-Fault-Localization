package com.powerfault.backend.config;

import com.powerfault.backend.entity.*;
import com.powerfault.backend.enums.DeviceStatus;
import com.powerfault.backend.enums.FeederStatus;
import com.powerfault.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final SubstationRepository substationRepository;
    private final FeederRepository feederRepository;
    private final TransformerRepository transformerRepository;
    private final PoleRepository poleRepository;
    private final DeviceRepository deviceRepository;
    private final DeviceTelemetryRepository deviceTelemetryRepository;
    private final NetworkConnectionRepository networkConnectionRepository;

    @Override
    public void run(String... args) throws Exception {

        if (substationRepository.count() > 0) {
            return;
        }

        System.out.println("==================================");
        System.out.println("Seeding synthetic power network...");
        System.out.println("==================================");

        Substation substation = Substation.builder()
                .substationCode("SS-001")
                .substationName("KSPDB South Division Substation")
                .latitude(12.9716)
                .longitude(77.5946)
                .build();
        substationRepository.save(substation);

        Feeder feeder1 = Feeder.builder()
                .feederCode("F-07-01")
                .feederName("Jayanagar 4th Block Feeder")
                .status(FeederStatus.ACTIVE)
                .substation(substation)
                .build();

        Feeder feeder2 = Feeder.builder()
                .feederCode("F-07-02")
                .feederName("JP Nagar 2nd Phase Feeder")
                .status(FeederStatus.ACTIVE)
                .substation(substation)
                .build();

        feederRepository.save(feeder1);
        feederRepository.save(feeder2);

        // DTs for Feeder 1
        Transformer dt111 = createDT("D-0111", "DT-111 Jayanagar East", 12.9682, 77.5946, true, feeder1);
        Transformer dt112 = createDT("D-0112", "DT-112 Jayanagar Central", 12.9689, 77.5943, true, feeder1);
        Transformer dt113 = createDT("D-0113", "DT-113 Jayanagar West", 12.9695, 77.5939, false, feeder1); // 60% case

        // DTs for Feeder 2
        Transformer dt114 = createDT("D-0114", "DT-114 JP Nagar North", 12.9650, 77.5910, true, feeder2);
        Transformer dt115 = createDT("D-0115", "DT-115 JP Nagar South", 12.9642, 77.5905, false, feeder2); // 60% case
        Transformer dt116 = createDT("D-0116", "DT-116 JP Nagar Market", 12.9635, 77.5898, false, feeder2); // 60% case

        // Seed Poles & Devices for each DT
        seedPolesForDT(dt111, "P-0111", 12, true);
        seedPolesForDT(dt112, "P-0112", 10, true);
        seedPolesForDT(dt113, "P-0113", 12, false); // Unknown topology
        seedPolesForDT(dt114, "P-0114", 10, true);
        seedPolesForDT(dt115, "P-0115", 10, false); // Unknown topology
        seedPolesForDT(dt116, "P-0116", 10, false); // Unknown topology

        System.out.println("✓ Synthetic power network seeded successfully!");
        System.out.println("==================================");
    }

    private Transformer createDT(String code, String name, double lat, double lon, boolean topologyKnown, Feeder feeder) {
        Transformer dt = Transformer.builder()
                .transformerCode(code)
                .transformerName(name)
                .latitude(lat)
                .longitude(lon)
                .topologyKnown(topologyKnown)
                .feeder(feeder)
                .build();
        return transformerRepository.save(dt);
    }

    private void seedPolesForDT(Transformer dt, String prefix, int count, boolean knownTopology) {
        Pole prevPole = null;

        for (int i = 1; i <= count; i++) {
            String poleCode = String.format("%s-%02d", prefix, i);
            double deltaLat = (i - 1) * 0.0004;
            double deltaLon = (i - 1) * 0.0003;
            double lat = dt.getLatitude() + deltaLat;
            double lon = dt.getLongitude() + deltaLon;

            // ~9% of poles have no device (e.g. pole #7 in line)
            boolean hasDevice = (i != 7);
            String fw = (i == 3) ? "1.2.0" : "1.4.2"; // ~8% on FW 1.2

            Pole pole = Pole.builder()
                    .poleCode(poleCode)
                    .latitude(lat)
                    .longitude(lon)
                    .sequenceNumber(knownTopology ? i : null)
                    .parentPoleCode(knownTopology && prevPole != null ? prevPole.getPoleCode() : null)
                    .hasDevice(hasDevice)
                    .ward("W-084")
                    .pincode("560078")
                    .transformer(dt)
                    .build();

            pole = poleRepository.save(pole);

            // If topology is known and prevPole exists, create NetworkConnection edge
            if (knownTopology && prevPole != null) {
                NetworkConnection conn = NetworkConnection.builder()
                        .fromPole(prevPole)
                        .toPole(pole)
                        .distanceMeters(45.0)
                        .active(true)
                        .build();
                networkConnectionRepository.save(conn);
            }

            if (hasDevice) {
                String deviceId = "KSPDB-DEV-" + poleCode;
                Device device = Device.builder()
                        .deviceId(deviceId)
                        .firmwareVersion(fw)
                        .lastSeen(LocalDateTime.now())
                        .status(DeviceStatus.ONLINE)
                        .pole(pole)
                        .build();

                device = deviceRepository.save(device);

                DeviceTelemetry telemetry = DeviceTelemetry.builder()
                        .device(device)
                        .energized(true)
                        .event("boot")
                        .sequenceNumber(1)
                        .batteryMv(3600)
                        .rssi(-75)
                        .timestamp(LocalDateTime.now())
                        .build();

                deviceTelemetryRepository.save(telemetry);
                device.setLatestTelemetry(telemetry);
                deviceRepository.save(device);
            }

            prevPole = pole;
        }

        // Add a branch spur for known topology DT if count >= 10
        if (knownTopology && count >= 10 && prevPole != null) {
            String branchPoleCode = prefix + "-05B";
            Pole junctionPole = poleRepository.findByPoleCode(prefix + "-05").orElse(prevPole);
            Pole branchPole = Pole.builder()
                    .poleCode(branchPoleCode)
                    .latitude(junctionPole.getLatitude() + 0.0003)
                    .longitude(junctionPole.getLongitude() - 0.0004)
                    .sequenceNumber(6)
                    .parentPoleCode(junctionPole.getPoleCode())
                    .hasDevice(true)
                    .ward("W-084")
                    .pincode("560078")
                    .transformer(dt)
                    .build();
            branchPole = poleRepository.save(branchPole);

            NetworkConnection branchConn = NetworkConnection.builder()
                    .fromPole(junctionPole)
                    .toPole(branchPole)
                    .distanceMeters(50.0)
                    .active(true)
                    .build();
            networkConnectionRepository.save(branchConn);

            Device device = Device.builder()
                    .deviceId("KSPDB-DEV-" + branchPoleCode)
                    .firmwareVersion("1.4.2")
                    .lastSeen(LocalDateTime.now())
                    .status(DeviceStatus.ONLINE)
                    .pole(branchPole)
                    .build();
            device = deviceRepository.save(device);

            DeviceTelemetry telemetry = DeviceTelemetry.builder()
                    .device(device)
                    .energized(true)
                    .event("boot")
                    .sequenceNumber(1)
                    .batteryMv(3600)
                    .rssi(-75)
                    .timestamp(LocalDateTime.now())
                    .build();
            deviceTelemetryRepository.save(telemetry);
            device.setLatestTelemetry(telemetry);
            deviceRepository.save(device);
        }
    }
}