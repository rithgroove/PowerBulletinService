package com.nopunnygames.pbservice.repository;

import com.nopunnygames.pbservice.entity.DeckIdentity;
import com.nopunnygames.tanuki.core.repository.MasterRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for deck identity records.
 */
public interface DeckIdentityRepository extends MasterRepository<DeckIdentity, UUID> {
    /**
     * Finds a deck identity by stable code.
     *
     * @param code stable code
     * @return matching identity, if present
     */
    Optional<DeckIdentity> findByCode(String code);
}
