package com.nopunnygames.pbservice.dto;

import com.nopunnygames.pbservice.enums.Faction;
import lombok.Data;

import java.util.List;
import java.util.UUID;

/**
 * DTO for the stable conceptual identity of a Power Bulletin card.
 */
@Data
public class CardIdentityDto {
    private UUID id;
    private String code;
    private String name;
    private String characterName;
    private Faction faction;
    private String notes;
    private String status = "Active";
    private List<CardVersionDto> versions;

    /**
     * Creates an empty card identity DTO.
     */
    public CardIdentityDto() {
    }
}
