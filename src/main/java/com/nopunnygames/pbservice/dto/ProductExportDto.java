package com.nopunnygames.pbservice.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Export shape for products and their print set quantities.
 */
@Data
public class ProductExportDto {
    private List<ProductDto> products = new ArrayList<>();

    /**
     * Creates an empty product export DTO.
     */
    public ProductExportDto() {
    }
}
