package com.powerfault.backend.config;

import com.powerfault.backend.entity.Feeder;
import com.powerfault.backend.entity.Substation;
import com.powerfault.backend.enums.FeederStatus;
import com.powerfault.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final SubstationRepository substationRepository;
    private final FeederRepository feederRepository;
    private final TransformerRepository transformerRepository;
    private final PoleRepository poleRepository;
    private final DeviceRepository deviceRepository;
    private final NetworkConnectionRepository networkConnectionRepository;

    @Override
    public void run(String... args) throws Exception {


        if (substationRepository.count() > 0) {
            return;
        }

        Substation substation = Substation.builder()
                .substationCode("SS-001")
                .substationName("KSPDB South Division")
                .latitude(12.9716)
                .longitude(77.5946)
                .build();

        substationRepository.save(substation);

        Feeder feeder1 = Feeder.builder()
                .feederCode("F-001")
                .feederName("South Feeder")
                .status(FeederStatus.ACTIVE)
                .substation(substation)
                .build();

        Feeder feeder2 = Feeder.builder()
                .feederCode("F-002")
                .feederName("East Feeder")
                .status(FeederStatus.ACTIVE)
                .substation(substation)
                .build();

        feederRepository.save(feeder1);
        feederRepository.save(feeder2);

        System.out.println("✓ Feeders created");

        System.out.println("✓ Substation created");

        System.out.println("==================================");
        System.out.println("Seeding synthetic power network...");
        System.out.println("==================================");

        // We will add the seed logic next.
    }
}