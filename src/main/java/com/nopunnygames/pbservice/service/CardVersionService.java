package com.nopunnygames.pbservice.service;

import com.nopunnygames.pbservice.dto.CardVersionDto;
import com.nopunnygames.pbservice.entity.CardIdentity;
import com.nopunnygames.pbservice.entity.CardVersion;
import com.nopunnygames.pbservice.entity.EffectDefinition;
import com.nopunnygames.pbservice.repository.CardIdentityRepository;
import com.nopunnygames.pbservice.repository.CardVersionRepository;
import com.nopunnygames.pbservice.repository.EffectDefinitionRepository;
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
 * Service for card version CRUD operations.
 */
@Service
public class CardVersionService extends MasterService<CardVersion, UUID, CardVersionDto> {
    private final CardVersionRepository repository;
    private final CardIdentityRepository cardIdentityRepository;
    private final EffectDefinitionRepository effectDefinitionRepository;

    /**
     * Creates the card version service.
     *
     * @param repository card version repository
     * @param cardIdentityRepository card identity repository
     * @param effectDefinitionRepository effect definition repository
     */
    public CardVersionService(
            CardVersionRepository repository,
            CardIdentityRepository cardIdentityRepository,
            EffectDefinitionRepository effectDefinitionRepository
    ) {
        this.repository = repository;
        this.cardIdentityRepository = cardIdentityRepository;
        this.effectDefinitionRepository = effectDefinitionRepository;
    }

    @Override
    protected CardVersionRepository getRepository() {
        return repository;
    }

    @Override
    protected Class<CardVersion> getEntityClass() {
        return CardVersion.class;
    }

    @Override
    protected Class<CardVersionDto> getDtoClass() {
        return CardVersionDto.class;
    }

    @Override
    protected List<ValidationError> createEntityValidation(CardVersionDto dto) {
        return validate(dto);
    }

    @Override
    protected List<ValidationError> updateEntityValidation(CardVersionDto dto) {
        return validate(dto);
    }

    @Override
    @Transactional
    public CardVersionDto create(CardVersionDto dto, AuthUser user) {
        List<ValidationError> errors = createEntityValidation(dto);
        if (!errors.isEmpty()) throw new ValidationErrorException("Creation Failed", errors);

        CardIdentity cardIdentity = cardIdentityRepository.findById(dto.getCardIdentityId())
                .orElseThrow(() -> new ObjectNotFoundException("Card identity " + dto.getCardIdentityId() + " not found"));
        EffectDefinition effectDefinition = effectDefinitionRepository.findById(dto.getEffectDefinitionId())
                .orElseThrow(() -> new ObjectNotFoundException("Effect definition " + dto.getEffectDefinitionId() + " not found"));

        CardVersion entity = new CardVersion();
        fillEntity(entity, dto, cardIdentity, effectDefinition);
        entity.setStatus(defaultStatus(dto.getStatus(), "Draft"));
        return repository.save(entity).toCompleteDto();
    }

    @Override
    @Transactional
    public CardVersionDto update(CardVersionDto dto, UUID id, AuthUser user) {
        List<ValidationError> errors = updateEntityValidation(dto);
        if (!errors.isEmpty()) throw new ValidationErrorException("Update Failed", errors);

        CardVersion entity = repository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Card version " + id + " not found"));
        CardIdentity cardIdentity = cardIdentityRepository.findById(dto.getCardIdentityId())
                .orElseThrow(() -> new ObjectNotFoundException("Card identity " + dto.getCardIdentityId() + " not found"));
        EffectDefinition effectDefinition = effectDefinitionRepository.findById(dto.getEffectDefinitionId())
                .orElseThrow(() -> new ObjectNotFoundException("Effect definition " + dto.getEffectDefinitionId() + " not found"));

        fillEntity(entity, dto, cardIdentity, effectDefinition);
        entity.setStatus(defaultStatus(dto.getStatus(), entity.getStatus()));
        return repository.save(entity).toCompleteDto();
    }

    /**
     * Lists versions for a card identity.
     *
     * @param cardIdentityId card identity UUID
     * @return matching version DTOs
     */
    public List<CardVersionDto> listByCardIdentity(UUID cardIdentityId) {
        return repository.findByCardIdentityIdOrderByVersionNameAscCodeAsc(cardIdentityId)
                .stream()
                .map(CardVersion::toCompleteDto)
                .toList();
    }

    private void fillEntity(CardVersion entity, CardVersionDto dto, CardIdentity cardIdentity, EffectDefinition effectDefinition) {
        entity.setCode(dto.getCode().trim());
        entity.setCardIdentity(cardIdentity);
        entity.setEffectDefinition(effectDefinition);
        entity.setVersionName(dto.getVersionName().trim());
        entity.setCardType(dto.getCardType());
        entity.setEffectText(dto.getEffectText().trim());
    }

    private List<ValidationError> validate(CardVersionDto dto) {
        List<ValidationError> errors = new ArrayList<>();
        if (dto.getCardIdentityId() == null) errors.add(new ValidationError("cardIdentityId", "Card identity is required."));
        if (dto.getEffectDefinitionId() == null) errors.add(new ValidationError("effectDefinitionId", "Effect definition is required."));
        if (isBlank(dto.getCode())) errors.add(new ValidationError("code", "Code is required."));
        if (isBlank(dto.getVersionName())) errors.add(new ValidationError("versionName", "Version name is required."));
        if (dto.getCardType() == null) errors.add(new ValidationError("cardType", "Card type is required."));
        if (isBlank(dto.getEffectText())) errors.add(new ValidationError("effectText", "Effect text is required."));
        return errors;
    }

    private String defaultStatus(String status, String fallback) {
        return isBlank(status) ? fallback : status;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
