package com.nopunnygames.pbservice.entity;

import com.nopunnygames.pbservice.dto.ProductDto;
import com.nopunnygames.pbservice.enums.ProductReleaseStatus;
import com.nopunnygames.pbservice.enums.ProductType;
import com.nopunnygames.tanuki.core.entity.MasterEntity;
import com.nopunnygames.tanuki.core.filter.FilterConfig;
import com.nopunnygames.tanuki.core.filter.FilterType;
import com.nopunnygames.tanuki.core.filter.SourceType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Packaged group of card print sets.
 */
@Entity
@Table(name = "products")
@Getter
@Setter
public class Product extends MasterEntity<Product, ProductDto> {
    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false)
    private ProductType productType = ProductType.OTHER;

    @Enumerated(EnumType.STRING)
    @Column(name = "release_status", nullable = false)
    private ProductReleaseStatus releaseStatus = ProductReleaseStatus.DRAFT;

    @Column(name = "display_order", nullable = false)
    private int displayOrder = 0;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    @OrderBy("sortOrder ASC, id ASC")
    private List<ProductItem> items = new ArrayList<>();

    /**
     * Creates an empty product entity.
     */
    public Product() {
    }

    @Override
    protected Class<ProductDto> getDtoClass() {
        return ProductDto.class;
    }

    @Override
    protected Class<Product> getEntityClass() {
        return Product.class;
    }

    @Override
    public ProductDto toDto() {
        ProductDto dto = new ProductDto();
        BeanUtils.copyProperties(this, dto, "items");
        return dto;
    }

    @Override
    public ProductDto toCompleteDto() {
        ProductDto dto = toDto();
        List<ProductItem> activeItems = items.stream()
                .filter(item -> item.getDeletedAt() == null)
                .filter(item -> "Active".equalsIgnoreCase(item.getStatus()))
                .toList();
        dto.setItems(activeItems.stream().map(ProductItem::toDto).toList());
        dto.setItemCount(activeItems.size());
        dto.setTotalQuantity(activeItems.stream().mapToInt(ProductItem::getQuantity).sum());
        return dto;
    }

    /**
     * Returns filter metadata for products.
     *
     * @return filter metadata
     */
    public static List<FilterConfig> getFilterConfigs() {
        return List.of(
                new FilterConfig("status", "Status", FilterType.CHECKBOXES, SourceType.DATABASE_DISTINCT, null, null, null, null, true),
                new FilterConfig("productType", "Product Type", FilterType.CHECKBOXES, SourceType.STATIC, null, List.of("CORE", "EXPANSION", "PROMO", "TEST_SET", "OTHER"), true),
                new FilterConfig("releaseStatus", "Release Status", FilterType.CHECKBOXES, SourceType.STATIC, null, List.of("DRAFT", "ACTIVE", "ARCHIVED"), true)
        );
    }

    /**
     * Returns fields used for keyword search.
     *
     * @return searchable fields
     */
    public static List<String> getSearchableFields() {
        return List.of("code", "name", "description", "productType", "releaseStatus");
    }
}
