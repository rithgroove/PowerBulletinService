package com.nopunnygames.pbservice.repository;

import com.nopunnygames.pbservice.entity.DeckVersion;
import com.nopunnygames.tanuki.core.repository.MasterRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for deck version records.
 */
public interface DeckVersionRepository extends MasterRepository<DeckVersion, UUID> {
    /**
     * Finds a deck version by stable code.
     *
     * @param code stable code
     * @return matching version, if present
     */
    Optional<DeckVersion> findByCode(String code);

    /**
     * Finds versions for one deck identity.
     *
     * @param deckIdentityId deck identity UUID
     * @return matching versions
     */
    List<DeckVersion> findByDeckIdentityIdOrderByVersionNameAscCodeAsc(UUID deckIdentityId);
}
