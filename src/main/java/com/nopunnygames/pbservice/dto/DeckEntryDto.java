package com.nopunnygames.pbservice.dto;

import lombok.Data;

import java.util.UUID;

/**
 * DTO linking a deck version to a card print set and quantity.
 */
@Data
public class DeckEntryDto {
    private UUID id;
    private UUID deckVersionId;
    private UUID cardPrintSetId;
    private int quantity = 1;
    private CardPrintSetDto printSet;

    /**
     * Creates an empty deck entry DTO.
     */
    public DeckEntryDto() {
    }
}
