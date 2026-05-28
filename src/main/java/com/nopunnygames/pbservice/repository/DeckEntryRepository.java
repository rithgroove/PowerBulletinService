package com.nopunnygames.pbservice.repository;

import com.nopunnygames.pbservice.entity.DeckEntry;
import com.nopunnygames.tanuki.core.repository.BaseRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for deck entry records.
 */
public interface DeckEntryRepository extends BaseRepository<DeckEntry, UUID> {
    /**
     * Finds entries for one deck version.
     *
     * @param deckVersionId deck version UUID
     * @return matching entries
     */
    List<DeckEntry> findByDeckVersionIdOrderByIdAsc(UUID deckVersionId);

    /**
     * Finds one entry for a deck version and print set.
     *
     * @param deckVersionId deck version UUID
     * @param cardPrintSetId card print set UUID
     * @return matching entry, if present
     */
    Optional<DeckEntry> findByDeckVersionIdAndCardPrintSetId(UUID deckVersionId, UUID cardPrintSetId);
}
