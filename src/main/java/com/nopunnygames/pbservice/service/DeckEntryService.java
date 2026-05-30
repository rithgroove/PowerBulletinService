package com.nopunnygames.pbservice.service;

import com.nopunnygames.pbservice.dto.DeckEntryDto;
import com.nopunnygames.pbservice.entity.CardPrintSet;
import com.nopunnygames.pbservice.entity.DeckEntry;
import com.nopunnygames.pbservice.entity.DeckVersion;
import com.nopunnygames.pbservice.repository.CardPrintSetRepository;
import com.nopunnygames.pbservice.repository.DeckEntryRepository;
import com.nopunnygames.pbservice.repository.DeckVersionRepository;
import com.nopunnygames.tanuki.core.dto.AuthUser;
import com.nopunnygames.tanuki.core.exception.ObjectNotFoundException;
import com.nopunnygames.tanuki.core.exception.ValidationError;
import com.nopunnygames.tanuki.core.exception.ValidationErrorException;
import com.nopunnygames.tanuki.core.repository.BaseRepository;
import com.nopunnygames.tanuki.core.service.BaseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service for deck entry CRUD operations.
 */
@Service
public class DeckEntryService extends BaseService<DeckEntry, UUID, DeckEntryDto> {
    private final DeckEntryRepository repository;
    private final DeckVersionRepository deckVersionRepository;
    private final CardPrintSetRepository cardPrintSetRepository;

    /**
     * Creates the deck entry service.
     *
     * @param repository deck entry repository
     * @param deckVersionRepository deck version repository
     * @param cardPrintSetRepository card print set repository
     */
    public DeckEntryService(
            DeckEntryRepository repository,
            DeckVersionRepository deckVersionRepository,
            CardPrintSetRepository cardPrintSetRepository
    ) {
        this.repository = repository;
        this.deckVersionRepository = deckVersionRepository;
        this.cardPrintSetRepository = cardPrintSetRepository;
    }

    @Override
    protected BaseRepository<DeckEntry, UUID> getRepository() {
        return repository;
    }

    @Override
    protected Class<DeckEntry> getEntityClass() {
        return DeckEntry.class;
    }

    @Override
    protected Class<DeckEntryDto> getDtoClass() {
        return DeckEntryDto.class;
    }

    @Override
    protected List<ValidationError> createEntityValidation(DeckEntryDto dto) {
        return validate(dto);
    }

    @Override
    protected List<ValidationError> updateEntityValidation(DeckEntryDto dto) {
        return validate(dto);
    }

    @Override
    @Transactional
    public DeckEntryDto create(DeckEntryDto dto, AuthUser user) {
        List<ValidationError> errors = createEntityValidation(dto);
        if (!errors.isEmpty()) throw new ValidationErrorException("Creation Failed", errors);

        DeckVersion deckVersion = deckVersionRepository.findById(dto.getDeckVersionId())
                .orElseThrow(() -> new ObjectNotFoundException("Deck version " + dto.getDeckVersionId() + " not found"));
        CardPrintSet cardPrintSet = cardPrintSetRepository.findById(dto.getCardPrintSetId())
                .orElseThrow(() -> new ObjectNotFoundException("Card print set " + dto.getCardPrintSetId() + " not found"));

        DeckEntry entity = new DeckEntry();
        fillEntity(entity, dto, deckVersion, cardPrintSet);
        return repository.save(entity).toCompleteDto();
    }

    @Override
    @Transactional
    public DeckEntryDto update(DeckEntryDto dto, UUID id, AuthUser user) {
        List<ValidationError> errors = updateEntityValidation(dto);
        if (!errors.isEmpty()) throw new ValidationErrorException("Update Failed", errors);

        DeckEntry entity = repository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Deck entry " + id + " not found"));
        DeckVersion deckVersion = deckVersionRepository.findById(dto.getDeckVersionId())
                .orElseThrow(() -> new ObjectNotFoundException("Deck version " + dto.getDeckVersionId() + " not found"));
        CardPrintSet cardPrintSet = cardPrintSetRepository.findById(dto.getCardPrintSetId())
                .orElseThrow(() -> new ObjectNotFoundException("Card print set " + dto.getCardPrintSetId() + " not found"));

        fillEntity(entity, dto, deckVersion, cardPrintSet);
        return repository.save(entity).toCompleteDto();
    }

    /**
     * Lists entries for a deck version.
     *
     * @param deckVersionId deck version UUID
     * @return matching entry DTOs
     */
    public List<DeckEntryDto> listByDeckVersion(UUID deckVersionId) {
        return repository.findByDeckVersionIdOrderByIdAsc(deckVersionId)
                .stream()
                .map(DeckEntry::toCompleteDto)
                .toList();
    }

    private void fillEntity(DeckEntry entity, DeckEntryDto dto, DeckVersion deckVersion, CardPrintSet cardPrintSet) {
        entity.setDeckVersion(deckVersion);
        entity.setCardPrintSet(cardPrintSet);
        entity.setQuantity(1);
    }

    private List<ValidationError> validate(DeckEntryDto dto) {
        List<ValidationError> errors = new ArrayList<>();
        if (dto.getDeckVersionId() == null) errors.add(new ValidationError("deckVersionId", "Deck version is required."));
        if (dto.getCardPrintSetId() == null) errors.add(new ValidationError("cardPrintSetId", "Card print set is required."));
        return errors;
    }
}
