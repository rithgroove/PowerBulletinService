package com.nopunnygames.pbservice.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Read-only product catalog summary.
 */
@Data
public class ProductSummaryDto {
    private UUID productId;
    private String code;
    private String name;
    private int cardPrintSetCount;
    private int totalCardQuantity;
    private Map<String, Integer> factionBreakdown = new LinkedHashMap<>();
    private Map<String, Integer> cardTypeBreakdown = new LinkedHashMap<>();
    private BigDecimal averagePower;
    private Integer minPower;
    private Integer maxPower;
    private List<ProductItemDto> includedCards;

    /**
     * Creates an empty product summary DTO.
     */
    public ProductSummaryDto() {
    }
}
