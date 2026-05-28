package com.nopunnygames.pbservice.repository;

import com.nopunnygames.pbservice.entity.CardIdentity;
import com.nopunnygames.tanuki.core.repository.MasterRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for card identity records.
 */
public interface CardIdentityRepository extends MasterRepository<CardIdentity, UUID> {
    /**
     * Finds a card identity by stable code.
     *
     * @param code stable code
     * @return matching identity, if present
     */
    Optional<CardIdentity> findByCode(String code);
}
