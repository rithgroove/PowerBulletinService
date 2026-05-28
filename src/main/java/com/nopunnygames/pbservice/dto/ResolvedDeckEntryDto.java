package com.nopunnygames.pbservice.dto;

import lombok.Data;

/**
 * Deck entry export with the full card identity, version, print set, and effect tree.
 */
@Data
public class ResolvedDeckEntryDto {
    private int quantity;
    private CardIdentityDto cardIdentity;
    private CardVersionDto cardVersion;
    private CardPrintSetDto cardPrintSet;
    private EffectDefinitionDto effectDefinition;

    /**
     * Creates an empty resolved deck entry DTO.
     */
    public ResolvedDeckEntryDto() {
    }
}
