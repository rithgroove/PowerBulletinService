package com.nopunnygames.pbservice.dto;

import lombok.Data;

import java.util.UUID;

/**
 * DTO for metadata that lets the Python simulator load an executable effect.
 */
@Data
public class EffectDefinitionDto {
    private UUID id;
    private String code;
    private String pythonClass;
    private String name;
    private String description;
    private String status = "Active";

    /**
     * Creates an empty effect definition DTO.
     */
    public EffectDefinitionDto() {
    }
}
