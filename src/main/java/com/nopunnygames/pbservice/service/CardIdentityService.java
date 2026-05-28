package com.nopunnygames.pbservice.service;

import com.nopunnygames.pbservice.dto.CardIdentityDto;
import com.nopunnygames.pbservice.entity.CardIdentity;
import com.nopunnygames.pbservice.repository.CardIdentityRepository;
import com.nopunnygames.tanuki.core.exception.ValidationError;
import com.nopunnygames.tanuki.core.service.MasterService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service for card identity CRUD operations.
 */
@Service
public class CardIdentityService extends MasterService<CardIdentity, UUID, CardIdentityDto> {
    private final CardIdentityRepository repository;

    /**
     * Creates the card identity service.
     *
     * @param repository card identity repository
     */
    public CardIdentityService(CardIdentityRepository repository) {
        this.repository = repository;
    }

    @Override
    protected CardIdentityRepository getRepository() {
        return repository;
    }

    @Override
    protected Class<CardIdentity> getEntityClass() {
        return CardIdentity.class;
    }

    @Override
    protected Class<CardIdentityDto> getDtoClass() {
        return CardIdentityDto.class;
    }

    @Override
    protected List<ValidationError> createEntityValidation(CardIdentityDto dto) {
        return validate(dto);
    }

    @Override
    protected List<ValidationError> updateEntityValidation(CardIdentityDto dto) {
        return validate(dto);
    }

    private List<ValidationError> validate(CardIdentityDto dto) {
        List<ValidationError> errors = new ArrayList<>();
        if (isBlank(dto.getCode())) errors.add(new ValidationError("code", "Code is required."));
        if (isBlank(dto.getName())) errors.add(new ValidationError("name", "Name is required."));
        if (isBlank(dto.getCharacterName())) errors.add(new ValidationError("characterName", "Character name is required."));
        if (dto.getFaction() == null) errors.add(new ValidationError("faction", "Faction is required."));
        return errors;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
