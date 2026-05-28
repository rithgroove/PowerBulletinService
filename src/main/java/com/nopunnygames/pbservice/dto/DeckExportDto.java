package com.nopunnygames.pbservice.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Simulator-facing export for a deck version and all resolved card metadata.
 */
@Data
public class DeckExportDto {
    private DeckIdentityDto deckIdentity;
    private DeckVersionDto deckVersion;
    private List<ResolvedDeckEntryDto> entries = new ArrayList<>();

    /**
     * Creates an empty deck export DTO.
     */
    public DeckExportDto() {
    }
}
