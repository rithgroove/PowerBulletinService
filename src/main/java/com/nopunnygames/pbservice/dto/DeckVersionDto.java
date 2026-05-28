package com.nopunnygames.pbservice.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

/**
 * DTO for one historical version of a deck.
 */
@Data
public class DeckVersionDto {
    private UUID id;
    private UUID deckIdentityId;
    private String code;
    private String versionName;
    private String notes;
    private String status = "Draft";
    private List<DeckEntryDto> entries;

    /**
     * Creates an empty deck version DTO.
     */
    public DeckVersionDto() {
    }
}
