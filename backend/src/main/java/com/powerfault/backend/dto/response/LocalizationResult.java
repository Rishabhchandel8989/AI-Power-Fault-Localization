package com.powerfault.backend.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocalizationResult {

    private String fromPole;

    private String toPole;

    private Double latitude;

    private Double longitude;

    private String pincode;

    private Integer affectedPoles;

    private Double confidence;

    private String reasoning;

    private boolean faultDetected;


}