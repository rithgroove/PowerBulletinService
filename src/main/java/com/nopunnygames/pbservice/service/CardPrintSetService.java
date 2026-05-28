package com.nopunnygames.pbservice.service;

import com.nopunnygames.pbservice.dto.CardPrintSetDto;
import com.nopunnygames.pbservice.entity.CardPrintSet;
import com.nopunnygames.pbservice.entity.CardVersion;
import com.nopunnygames.pbservice.repository.CardPrintSetRepository;
import com.nopunnygames.pbservice.repository.CardVersionRepository;
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
 * Service for card print set CRUD operations.
 */
@Service
public class CardPrintSetService extends MasterService<CardPrintSet, UUID, CardPrintSetDto> {
    private final CardPrintSetRepository repository;
    private final CardVersionRepository cardVersionRepository;

    /**
     * Creates the card print set service.
     *
     * @param repository card print set repository
     * @param cardVersionRepository card version repository
     */
    public CardPrintSetService(CardPrintSetRepository repository, CardVersionRepository cardVersionRepository) {
        this.repository = repository;
        this.cardVersionRepository = cardVersionRepository;
    }

    @Override
    protected CardPrintSetRepository getRepository() {
        return repository;
    }

    @Override
    protected Class<CardPrintSet> getEntityClass() {
        return CardPrintSet.class;
    }

    @Override
    protected Class<CardPrintSetDto> getDtoClass() {
        return CardPrintSetDto.class;
    }

    @Override
    protected List<ValidationError> createEntityValidation(CardPrintSetDto dto) {
        return validate(dto);
    }

    @Override
    protected List<ValidationError> updateEntityValidation(CardPrintSetDto dto) {
        return validate(dto);
    }

    @Override
    @Transactional
    public CardPrintSetDto create(CardPrintSetDto dto, AuthUser user) {
        List<ValidationError> errors = createEntityValidation(dto);
        if (!errors.isEmpty()) throw new ValidationErrorException("Creation Failed", errors);

        CardVersion cardVersion = cardVersionRepository.findById(dto.getCardVersionId())
                .orElseThrow(() -> new ObjectNotFoundException("Card version " + dto.getCardVersionId() + " not found"));

        CardPrintSet entity = new CardPrintSet();
        fillEntity(entity, dto, cardVersion);
        entity.setStatus(defaultStatus(dto.getStatus(), "Active"));
        return repository.save(entity).toCompleteDto();
    }

    @Override
    @Transactional
    public CardPrintSetDto update(CardPrintSetDto dto, UUID id, AuthUser user) {
        List<ValidationError> errors = updateEntityValidation(dto);
        if (!errors.isEmpty()) throw new ValidationErrorException("Update Failed", errors);

        CardPrintSet entity = repository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Card print set " + id + " not found"));
        CardVersion cardVersion = cardVersionRepository.findById(dto.getCardVersionId())
                .orElseThrow(() -> new ObjectNotFoundException("Card version " + dto.getCardVersionId() + " not found"));

        fillEntity(entity, dto, cardVersion);
        entity.setStatus(defaultStatus(dto.getStatus(), entity.getStatus()));
        return repository.save(entity).toCompleteDto();
    }

    /**
     * Lists print sets for a card version.
     *
     * @param cardVersionId card version UUID
     * @return matching print set DTOs
     */
    public List<CardPrintSetDto> listByCardVersion(UUID cardVersionId) {
        return repository.findByCardVersionIdOrderByCodeAsc(cardVersionId)
                .stream()
                .map(CardPrintSet::toCompleteDto)
                .toList();
    }

    private void fillEntity(CardPrintSet entity, CardPrintSetDto dto, CardVersion cardVersion) {
        entity.setCode(dto.getCode().trim());
        entity.setCardVersion(cardVersion);
        entity.replacePowers(dto.getPowers());
    }

    private List<ValidationError> validate(CardPrintSetDto dto) {
        List<ValidationError> errors = new ArrayList<>();
        if (dto.getCardVersionId() == null) errors.add(new ValidationError("cardVersionId", "Card version is required."));
        if (isBlank(dto.getCode())) errors.add(new ValidationError("code", "Code is required."));
        if (dto.getPowers() == null || dto.getPowers().isEmpty()) {
            errors.add(new ValidationError("powers", "At least one ordered power value is required."));
        }
        return errors;
    }

    private String defaultStatus(String status, String fallback) {
        return isBlank(status) ? fallback : status;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
