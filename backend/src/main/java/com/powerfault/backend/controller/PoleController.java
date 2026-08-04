package com.powerfault.backend.controller;

import com.powerfault.backend.entity.Pole;
import com.powerfault.backend.repository.PoleRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/poles")
@RequiredArgsConstructor
public class PoleController {

    private final PoleRepository poleRepository;

    @Data
    @Builder
    public static class PoleDTO {
        private Long id;
        private String poleCode;
        private Double latitude;
        private Double longitude;
        private Integer sequenceNumber;
        private String parentPoleCode;
        private Boolean hasDevice;
        private String ward;
        private String pincode;
        private String transformerCode;
        private String feederCode;
        private Boolean topologyKnown;
        private Boolean energized;
        private String deviceStatus;
        private String firmwareVersion;
    }

    @GetMapping
    public ResponseEntity<List<PoleDTO>> getAllPoles() {
        List<Pole> poles = poleRepository.findAll();
        List<PoleDTO> dtos = poles.stream().map(p -> {
            Boolean energized = true;
            String devStatus = "NO_DEVICE";
            String fw = "N/A";

            if (p.getDevice() != null) {
                devStatus = String.valueOf(p.getDevice().getStatus());
                fw = p.getDevice().getFirmwareVersion();
                if (p.getDevice().getLatestTelemetry() != null) {
                    energized = p.getDevice().getLatestTelemetry().getEnergized();
                }
            }

            return PoleDTO.builder()
                    .id(p.getId())
                    .poleCode(p.getPoleCode())
                    .latitude(p.getLatitude())
                    .longitude(p.getLongitude())
                    .sequenceNumber(p.getSequenceNumber())
                    .parentPoleCode(p.getParentPoleCode())
                    .hasDevice(p.getHasDevice())
                    .ward(p.getWard())
                    .pincode(p.getPincode())
                    .transformerCode(p.getTransformer().getTransformerCode())
                    .feederCode(p.getTransformer().getFeeder().getFeederCode())
                    .topologyKnown(p.getTransformer().getTopologyKnown())
                    .energized(energized)
                    .deviceStatus(devStatus)
                    .firmwareVersion(fw)
                    .build();
        }).toList();

        return ResponseEntity.ok(dtos);
    }
}
