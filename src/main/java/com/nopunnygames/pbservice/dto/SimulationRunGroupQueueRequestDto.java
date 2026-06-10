package com.nopunnygames.pbservice.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

/**
 * Request body for queueing a grouped simulator run.
 */
@Data
public class SimulationRunGroupQueueRequestDto {
    private UUID deckVersionId;
    private Long rngSeed;
    private Integer iterationsPerPlayerCount;
    private List<Integer> playerCounts;
    private String runGroupCode;
    private String runGroupName;
    private String notes;

    /**
     * Creates an empty queue request DTO.
     */
    public SimulationRunGroupQueueRequestDto() {
    }
}
