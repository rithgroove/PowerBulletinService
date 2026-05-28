package com.nopunnygames.pbservice.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DTO for one exact print distribution of a card version.
 */
@Data
public class CardPrintSetDto {
    private UUID id;
    private UUID cardVersionId;
    private String code;
    private String status = "Active";
    private List<Integer> powers = new ArrayList<>();

    /**
     * Creates an empty print set DTO.
     */
    public CardPrintSetDto() {
    }
}
