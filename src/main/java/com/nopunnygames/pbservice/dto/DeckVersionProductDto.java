package com.nopunnygames.pbservice.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO recording a product application to a deck version.
 */
@Data
public class DeckVersionProductDto {
    private UUID id;
    private UUID deckVersionId;
    private UUID productId;
    private String productCode;
    private String productName;
    private String productType;
    private String releaseStatus;
    private int quantityMultiplier = 1;
    private LocalDateTime appliedAt;
    private String status = "Active";

    /**
     * Creates an empty deck version product DTO.
     */
    public DeckVersionProductDto() {
    }
}
