package com.nopunnygames.pbservice.entity;

import com.nopunnygames.pbservice.dto.ProductItemDto;
import com.nopunnygames.tanuki.core.entity.MasterEntity;
import com.nopunnygames.tanuki.core.filter.FilterConfig;
import com.nopunnygames.tanuki.core.filter.FilterType;
import com.nopunnygames.tanuki.core.filter.SourceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.util.List;

/**
 * Product membership for one exact card print set.
 */
@Entity
@Table(name = "product_items")
@Getter
@Setter
public class ProductItem extends MasterEntity<ProductItem, ProductItemDto> {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card_print_set_id", nullable = false)
    private CardPrintSet cardPrintSet;

    @Column(nullable = false)
    private int quantity = 1;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    /**
     * Creates an empty product item entity.
     */
    public ProductItem() {
    }

    @Override
    protected Class<ProductItemDto> getDtoClass() {
        return ProductItemDto.class;
    }

    @Override
    protected Class<ProductItem> getEntityClass() {
        return ProductItem.class;
    }

    @Override
    public ProductItemDto toDto() {
        ProductItemDto dto = new ProductItemDto();
        BeanUtils.copyProperties(this, dto, "product", "cardPrintSet");
        if (product != null) {
            dto.setProductId(product.getId());
        }
        if (cardPrintSet != null) {
            dto.setCardPrintSetId(cardPrintSet.getId());
            dto.setPrintSetCode(cardPrintSet.getCode());
            dto.setPowers(cardPrintSet.getPowers().stream().map(CardPrintSetPower::getPower).toList());
            CardVersion version = cardPrintSet.getCardVersion();
            if (version != null) {
                dto.setCardVersionName(version.getVersionName());
                dto.setCardType(version.getCardType());
                dto.setEffectText(version.getEffectText());
                CardIdentity identity = version.getCardIdentity();
                if (identity != null) {
                    dto.setCardIdentityId(identity.getId());
                    dto.setCardIdentityName(identity.getName());
                    dto.setCharacterName(identity.getCharacterName());
                    dto.setFaction(identity.getFaction());
                }
            }
        }
        return dto;
    }

    /**
     * Returns filter metadata for product items.
     *
     * @return filter metadata
     */
    public static List<FilterConfig> getFilterConfigs() {
        return List.of(new FilterConfig("status", "Status", FilterType.CHECKBOXES, SourceType.DATABASE_DISTINCT, null, null, null, null, true));
    }

    /**
     * Returns no keyword search fields for product items.
     *
     * @return empty searchable field list
     */
    public static List<String> getSearchableFields() {
        return List.of();
    }
}
