package com.nopunnygames.pbservice.entity;

import com.nopunnygames.pbservice.dto.DeckVersionProductDto;
import com.nopunnygames.tanuki.core.entity.MasterEntity;
import com.nopunnygames.tanuki.core.filter.FilterConfig;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Records that a product was applied to a deck version.
 */
@Entity
@Table(name = "deck_version_products")
@Getter
@Setter
public class DeckVersionProduct extends MasterEntity<DeckVersionProduct, DeckVersionProductDto> {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "deck_version_id", nullable = false)
    private DeckVersion deckVersion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity_multiplier", nullable = false)
    private int quantityMultiplier = 1;

    @Column(name = "applied_at", nullable = false)
    private LocalDateTime appliedAt;

    /**
     * Creates an empty deck version product entity.
     */
    public DeckVersionProduct() {
    }

    @Override
    protected Class<DeckVersionProductDto> getDtoClass() {
        return DeckVersionProductDto.class;
    }

    @Override
    protected Class<DeckVersionProduct> getEntityClass() {
        return DeckVersionProduct.class;
    }

    @Override
    public DeckVersionProductDto toDto() {
        DeckVersionProductDto dto = new DeckVersionProductDto();
        BeanUtils.copyProperties(this, dto, "deckVersion", "product");
        if (deckVersion != null) {
            dto.setDeckVersionId(deckVersion.getId());
        }
        if (product != null) {
            dto.setProductId(product.getId());
        }
        return dto;
    }

    /**
     * Returns no filter metadata for provenance rows.
     *
     * @return empty filter metadata
     */
    public static List<FilterConfig> getFilterConfigs() {
        return List.of();
    }

    /**
     * Returns no keyword search fields for provenance rows.
     *
     * @return empty searchable field list
     */
    public static List<String> getSearchableFields() {
        return List.of();
    }
}
