package com.nopunnygames.pbservice.dto;

import lombok.Data;

import java.util.UUID;

/**
 * Summary returned after applying a product to a deck version.
 */
@Data
public class ProductDeckApplicationDto {
    private UUID deckVersionId;
    private UUID productId;
    private String productCode;
    private String productName;
    private int itemsAdded;
    private int entriesCreated;
    private int entriesUpdated;
    private int totalQuantityAdded;

    /**
     * Creates an empty application summary DTO.
     */
    public ProductDeckApplicationDto() {
    }
}
