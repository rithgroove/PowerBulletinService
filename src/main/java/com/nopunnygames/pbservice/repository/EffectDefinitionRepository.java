package com.nopunnygames.pbservice.repository;

import com.nopunnygames.pbservice.entity.EffectDefinition;
import com.nopunnygames.tanuki.core.repository.MasterRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for effect definition records.
 */
public interface EffectDefinitionRepository extends MasterRepository<EffectDefinition, UUID> {
    /**
     * Finds an effect definition by stable code.
     *
     * @param code stable code
     * @return matching effect definition, if present
     */
    Optional<EffectDefinition> findByCode(String code);
}
