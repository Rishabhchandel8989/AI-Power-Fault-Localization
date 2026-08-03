//package com.powerfault.backend.dto.request;
//import jakarta.validation.constraints.NotBlank;
//import jakarta.validation.constraints.NotNull;
//import lombok.*;
//
//import java.time.LocalDateTime;
//
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class TelemetryRequest {
//
////    private String deviceId;
////
////    private String poleId;
////
////    private String event;
////
////    private Boolean energized;
////
////    private LocalDateTime timestamp;
////
////    private Integer sequenceNumber;
////
////    private Integer batteryMv;
////
////    private Integer rssi;
//
//    @NotBlank
//    private String deviceId;
//
//    @NotBlank
//    private String poleId;
//
//    @NotBlank
//    private String event;
//
//    @NotNull
//    private Boolean energized;
//
//    @NotNull
//    private LocalDateTime timestamp;
//
//    @NotNull
//    private Integer sequenceNumber;
//
//}

package com.powerfault.backend.dto.request;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TelemetryRequest {

    private String deviceId;

    private String poleId;

    private String event;

    private Boolean energized;

    private LocalDateTime timestamp;

    private Integer sequenceNumber;

    private Integer batteryMv;

    private Integer rssi;
}