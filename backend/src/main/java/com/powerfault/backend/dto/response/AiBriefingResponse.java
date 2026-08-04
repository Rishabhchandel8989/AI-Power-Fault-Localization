package com.powerfault.backend.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiBriefingResponse {
    private Long incidentId;
    private String incidentNumber;
    private String severity;
    private String summary;
    private String locationDetails;
    private int estimatedHouseholdImpact;
    private int recommendedCrewSize;
    private List<String> materialsChecklist;
    private List<String> safetyDirectives;
    private String topologyNotes;
}
