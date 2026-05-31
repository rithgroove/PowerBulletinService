package com.nopunnygames.pbservice.repository;

import com.nopunnygames.pbservice.entity.DeckVersionProduct;
import com.nopunnygames.tanuki.core.repository.MasterRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for deck version product provenance rows.
 */
public interface DeckVersionProductRepository extends MasterRepository<DeckVersionProduct, UUID> {
    /**
     * Finds an active provenance row for a deck version and product pair.
     *
     * @param deckVersionId deck version UUID
     * @param productId product UUID
     * @return matching row, if present
     */
    @Query("""
            SELECT item
            FROM DeckVersionProduct item
            WHERE item.deckVersion.id = :deckVersionId
              AND item.product.id = :productId
              AND item.deletedAt IS NULL
              AND LOWER(item.status) = 'active'
            """)
    Optional<DeckVersionProduct> findActiveByDeckVersionIdAndProductId(
            @Param("deckVersionId") UUID deckVersionId,
            @Param("productId") UUID productId
    );

    /**
     * Lists active product links for one deck version.
     *
     * @param deckVersionId deck version UUID
     * @return active product links
     */
    @Query("""
            SELECT item
            FROM DeckVersionProduct item
            JOIN FETCH item.product product
            WHERE item.deckVersion.id = :deckVersionId
              AND item.deletedAt IS NULL
              AND LOWER(item.status) = 'active'
            ORDER BY product.displayOrder ASC, LOWER(product.name) ASC, LOWER(product.code) ASC
            """)
    List<DeckVersionProduct> findActiveByDeckVersionId(@Param("deckVersionId") UUID deckVersionId);
}
