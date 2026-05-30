package com.nopunnygames.pbservice.repository;

import com.nopunnygames.pbservice.entity.ProductItem;
import com.nopunnygames.tanuki.core.repository.MasterRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for product item records.
 */
public interface ProductItemRepository extends MasterRepository<ProductItem, UUID> {
    /**
     * Lists active product items in display order.
     *
     * @param productId product UUID
     * @return active product item rows
     */
    @Query("""
            SELECT item
            FROM ProductItem item
            WHERE item.product.id = :productId
              AND item.deletedAt IS NULL
              AND LOWER(item.status) = 'active'
            ORDER BY item.sortOrder ASC, item.id ASC
            """)
    List<ProductItem> findActiveByProductId(@Param("productId") UUID productId);

    /**
     * Finds an active product item for a product and print set pair.
     *
     * @param productId product UUID
     * @param cardPrintSetId card print set UUID
     * @return matching active product item, if present
     */
    @Query("""
            SELECT item
            FROM ProductItem item
            WHERE item.product.id = :productId
              AND item.cardPrintSet.id = :cardPrintSetId
              AND item.deletedAt IS NULL
              AND LOWER(item.status) = 'active'
            """)
    Optional<ProductItem> findActiveByProductIdAndCardPrintSetId(
            @Param("productId") UUID productId,
            @Param("cardPrintSetId") UUID cardPrintSetId
    );
}
