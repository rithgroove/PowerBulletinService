package com.nopunnygames.pbservice.service;

import com.nopunnygames.pbservice.dto.DeckIdentityDto;
import com.nopunnygames.pbservice.entity.DeckIdentity;
import com.nopunnygames.pbservice.repository.DeckIdentityRepository;
import com.nopunnygames.tanuki.core.exception.ValidationError;
import com.nopunnygames.tanuki.core.service.MasterService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service for deck identity CRUD operations.
 */
@Service
public class DeckIdentityService extends MasterService<DeckIdentity, UUID, DeckIdentityDto> {
    private final DeckIdentityRepository repository;

    /**
     * Creates the deck identity service.
     *
     * @param repository deck identity repository
     */
    public DeckIdentityService(DeckIdentityRepository repository) {
        this.repository = repository;
    }

    @Override
    protected DeckIdentityRepository getRepository() {
        return repository;
    }

    @Override
    protected Class<DeckIdentity> getEntityClass() {
        return DeckIdentity.class;
    }

    @Override
    protected Class<DeckIdentityDto> getDtoClass() {
        return DeckIdentityDto.class;
    }

    @Override
    protected List<ValidationError> createEntityValidation(DeckIdentityDto dto) {
        return validate(dto);
    }

    @Override
    protected List<ValidationError> updateEntityValidation(DeckIdentityDto dto) {
        return validate(dto);
    }

    private List<ValidationError> validate(DeckIdentityDto dto) {
        List<ValidationError> errors = new ArrayList<>();
        if (isBlank(dto.getCode())) errors.add(new ValidationError("code", "Code is required."));
        if (isBlank(dto.getName())) errors.add(new ValidationError("name", "Name is required."));
        return errors;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
