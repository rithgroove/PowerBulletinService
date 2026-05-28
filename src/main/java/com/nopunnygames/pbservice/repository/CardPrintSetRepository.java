package com.nopunnygames.pbservice.repository;

import com.nopunnygames.pbservice.entity.CardPrintSet;
import com.nopunnygames.tanuki.core.repository.MasterRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for card print set records.
 */
public interface CardPrintSetRepository extends MasterRepository<CardPrintSet, UUID> {
    /**
     * Finds a print set by stable code.
     *
     * @param code stable code
     * @return matching print set, if present
     */
    Optional<CardPrintSet> findByCode(String code);

    /**
     * Finds print sets for one card version.
     *
     * @param cardVersionId card version UUID
     * @return matching print sets
     */
    List<CardPrintSet> findByCardVersionIdOrderByCodeAsc(UUID cardVersionId);
}
