package com.nopunnygames.pbservice.dto;

import com.nopunnygames.pbservice.enums.CardType;
import lombok.Data;

import java.util.List;
import java.util.UUID;

/**
 * DTO for one gameplay/rules version of a card identity.
 */
@Data
public class CardVersionDto {
    private UUID id;
    private UUID cardIdentityId;
    private UUID effectDefinitionId;
    private String code;
    private String versionName;
    private CardType cardType;
    private String effectText;
    private String status = "Draft";
    private EffectDefinitionDto effectDefinition;
    private List<CardPrintSetDto> printSets;

    /**
     * Creates an empty card version DTO.
     */
    public CardVersionDto() {
    }
}
