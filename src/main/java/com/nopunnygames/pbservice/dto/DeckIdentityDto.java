package com.nopunnygames.pbservice.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

/**
 * DTO for a stable conceptual deck identity.
 */
@Data
public class DeckIdentityDto {
    private UUID id;
    private String code;
    private String name;
    private String notes;
    private String status = "Active";
    private List<DeckVersionDto> versions;

    /**
     * Creates an empty deck identity DTO.
     */
    public DeckIdentityDto() {
    }
}
