package com.nopunnygames.pbservice.service;

import com.nopunnygames.pbservice.dto.DeckVersionDto;
import com.nopunnygames.pbservice.entity.DeckIdentity;
import com.nopunnygames.pbservice.entity.DeckVersion;
import com.nopunnygames.pbservice.repository.DeckIdentityRepository;
import com.nopunnygames.pbservice.repository.DeckVersionRepository;
import com.nopunnygames.tanuki.core.dto.AuthUser;
import com.nopunnygames.tanuki.core.exception.ObjectNotFoundException;
import com.nopunnygames.tanuki.core.exception.ValidationError;
import com.nopunnygames.tanuki.core.exception.ValidationErrorException;
import com.nopunnygames.tanuki.core.service.MasterService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service for deck version CRUD operations.
 */
@Service
public class DeckVersionService extends MasterService<DeckVersion, UUID, DeckVersionDto> {
    private final DeckVersionRepository repository;
    private final DeckIdentityRepository deckIdentityRepository;

    /**
     * Creates the deck version service.
     *
     * @param repository deck version repository
     * @param deckIdentityRepository deck identity repository
     */
    public DeckVersionService(DeckVersionRepository repository, DeckIdentityRepository deckIdentityRepository) {
        this.repository = repository;
        this.deckIdentityRepository = deckIdentityRepository;
    }

    @Override
    protected DeckVersionRepository getRepository() {
        return repository;
    }

    @Override
    protected Class<DeckVersion> getEntityClass() {
        return DeckVersion.class;
    }

    @Override
    protected Class<DeckVersionDto> getDtoClass() {
        return DeckVersionDto.class;
    }

    @Override
    protected List<ValidationError> createEntityValidation(DeckVersionDto dto) {
        return validate(dto);
    }

    @Override
    protected List<ValidationError> updateEntityValidation(DeckVersionDto dto) {
        return validate(dto);
    }

    @Override
    @Transactional
    public DeckVersionDto create(DeckVersionDto dto, AuthUser user) {
        List<ValidationError> errors = createEntityValidation(dto);
        if (!errors.isEmpty()) throw new ValidationErrorException("Creation Failed", errors);

        DeckIdentity deckIdentity = deckIdentityRepository.findById(dto.getDeckIdentityId())
                .orElseThrow(() -> new ObjectNotFoundException("Deck identity " + dto.getDeckIdentityId() + " not found"));
        DeckVersion entity = new DeckVersion();
        fillEntity(entity, dto, deckIdentity);
        entity.setStatus(defaultStatus(dto.getStatus(), "Draft"));
        return repository.save(entity).toCompleteDto();
    }

    @Override
    @Transactional
    public DeckVersionDto update(DeckVersionDto dto, UUID id, AuthUser user) {
        List<ValidationError> errors = updateEntityValidation(dto);
        if (!errors.isEmpty()) throw new ValidationErrorException("Update Failed", errors);

        DeckVersion entity = repository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Deck version " + id + " not found"));
        DeckIdentity deckIdentity = deckIdentityRepository.findById(dto.getDeckIdentityId())
                .orElseThrow(() -> new ObjectNotFoundException("Deck identity " + dto.getDeckIdentityId() + " not found"));
        fillEntity(entity, dto, deckIdentity);
        entity.setStatus(defaultStatus(dto.getStatus(), entity.getStatus()));
        return repository.save(entity).toCompleteDto();
    }

    /**
     * Lists versions for a deck identity.
     *
     * @param deckIdentityId deck identity UUID
     * @return matching version DTOs
     */
    public List<DeckVersionDto> listByDeckIdentity(UUID deckIdentityId) {
        return repository.findByDeckIdentityIdOrderByVersionNameAscCodeAsc(deckIdentityId)
                .stream()
                .map(DeckVersion::toCompleteDto)
                .toList();
    }

    private void fillEntity(DeckVersion entity, DeckVersionDto dto, DeckIdentity deckIdentity) {
        entity.setCode(dto.getCode().trim());
        entity.setDeckIdentity(deckIdentity);
        entity.setVersionName(dto.getVersionName().trim());
        entity.setNotes(dto.getNotes());
    }

    private List<ValidationError> validate(DeckVersionDto dto) {
        List<ValidationError> errors = new ArrayList<>();
        if (dto.getDeckIdentityId() == null) errors.add(new ValidationError("deckIdentityId", "Deck identity is required."));
        if (isBlank(dto.getCode())) errors.add(new ValidationError("code", "Code is required."));
        if (isBlank(dto.getVersionName())) errors.add(new ValidationError("versionName", "Version name is required."));
        return errors;
    }

    private String defaultStatus(String status, String fallback) {
        return isBlank(status) ? fallback : status;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
