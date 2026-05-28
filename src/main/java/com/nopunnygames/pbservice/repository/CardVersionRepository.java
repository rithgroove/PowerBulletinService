package com.nopunnygames.pbservice.repository;

import com.nopunnygames.pbservice.entity.CardVersion;
import com.nopunnygames.tanuki.core.repository.MasterRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for card version records.
 */
public interface CardVersionRepository extends MasterRepository<CardVersion, UUID> {
    /**
     * Finds a card version by stable code.
     *
     * @param code stable code
     * @return matching version, if present
     */
    Optional<CardVersion> findByCode(String code);

    /**
     * Finds versions for one card identity.
     *
     * @param cardIdentityId card identity UUID
     * @return matching versions
     */
    List<CardVersion> findByCardIdentityIdOrderByVersionNameAscCodeAsc(UUID cardIdentityId);
}
