package com.nopunnygames.pbservice.dto;

import com.nopunnygames.pbservice.enums.ProductReleaseStatus;
import com.nopunnygames.pbservice.enums.ProductType;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DTO for a packaged group of card print sets.
 */
@Data
public class ProductDto {
    private UUID id;
    private String code;
    private String name;
    private String description;
    private ProductType productType = ProductType.OTHER;
    private ProductReleaseStatus releaseStatus = ProductReleaseStatus.DRAFT;
    private int displayOrder = 0;
    private String status = "Active";
    private int itemCount;
    private int totalQuantity;
    private List<ProductItemDto> items = new ArrayList<>();

    /**
     * Creates an empty product DTO.
     */
    public ProductDto() {
    }
}
