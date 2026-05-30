package com.nopunnygames.pbservice.dto;

import com.nopunnygames.pbservice.enums.CardType;
import com.nopunnygames.pbservice.enums.Faction;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DTO linking a product to one card print set.
 */
@Data
public class ProductItemDto {
    private UUID id;
    private UUID productId;
    private UUID cardPrintSetId;
    private int quantity = 1;
    private int sortOrder = 0;
    private String status = "Active";
    private String printSetCode;
    private String cardIdentityName;
    private String characterName;
    private Faction faction;
    private String cardVersionName;
    private CardType cardType;
    private String effectText;
    private List<Integer> powers = new ArrayList<>();

    /**
     * Creates an empty product item DTO.
     */
    public ProductItemDto() {
    }
}
