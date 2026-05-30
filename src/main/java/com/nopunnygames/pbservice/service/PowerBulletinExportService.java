package com.nopunnygames.pbservice.service;

import com.nopunnygames.pbservice.dto.CardIdentityDto;
import com.nopunnygames.pbservice.dto.DeckExportDto;
import com.nopunnygames.pbservice.dto.ProductExportDto;
import com.nopunnygames.pbservice.dto.ResolvedDeckEntryDto;
import com.nopunnygames.pbservice.entity.CardIdentity;
import com.nopunnygames.pbservice.entity.DeckEntry;
import com.nopunnygames.pbservice.entity.DeckVersion;
import com.nopunnygames.pbservice.repository.CardIdentityRepository;
import com.nopunnygames.pbservice.repository.DeckEntryRepository;
import com.nopunnygames.pbservice.repository.DeckVersionRepository;
import com.nopunnygames.tanuki.core.exception.ObjectNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Read service for simulator-facing resolved exports.
 */
@Service
public class PowerBulletinExportService {
    private final CardIdentityRepository cardIdentityRepository;
    private final DeckVersionRepository deckVersionRepository;
    private final DeckEntryRepository deckEntryRepository;
    private final ProductService productService;

    /**
     * Creates the export service.
     *
     * @param cardIdentityRepository card identity repository
     * @param deckVersionRepository deck version repository
     * @param deckEntryRepository deck entry repository
     * @param productService product service
     */
    public PowerBulletinExportService(
            CardIdentityRepository cardIdentityRepository,
            DeckVersionRepository deckVersionRepository,
            DeckEntryRepository deckEntryRepository,
            ProductService productService
    ) {
        this.cardIdentityRepository = cardIdentityRepository;
        this.deckVersionRepository = deckVersionRepository;
        this.deckEntryRepository = deckEntryRepository;
        this.productService = productService;
    }

    /**
     * Returns all cards with versions, print sets, ordered powers, and effects.
     *
     * @return complete card tree
     */
    @Transactional(readOnly = true)
    public List<CardIdentityDto> getCardTree() {
        return cardIdentityRepository.findAll()
                .stream()
                .map(CardIdentity::toCompleteDto)
                .toList();
    }

    /**
     * Returns a resolved deck export by deck version UUID.
     *
     * @param deckVersionId deck version UUID
     * @return resolved deck export
     */
    @Transactional(readOnly = true)
    public DeckExportDto getDeckExport(UUID deckVersionId) {
        DeckVersion deckVersion = deckVersionRepository.findById(deckVersionId)
                .orElseThrow(() -> new ObjectNotFoundException("Deck version " + deckVersionId + " not found"));
        return buildDeckExport(deckVersion);
    }

    /**
     * Returns a resolved deck export by deck version code.
     *
     * @param code deck version code
     * @return resolved deck export
     */
    @Transactional(readOnly = true)
    public DeckExportDto getDeckExportByCode(String code) {
        DeckVersion deckVersion = deckVersionRepository.findByCode(code)
                .orElseThrow(() -> new ObjectNotFoundException("Deck version " + code + " not found"));
        return buildDeckExport(deckVersion);
    }

    /**
     * Returns active products with resolved product items.
     *
     * @return product export DTO
     */
    @Transactional(readOnly = true)
    public ProductExportDto getProductExport() {
        ProductExportDto export = new ProductExportDto();
        export.setProducts(productService.exportProducts());
        return export;
    }

    private DeckExportDto buildDeckExport(DeckVersion deckVersion) {
        DeckExportDto export = new DeckExportDto();
        export.setDeckIdentity(deckVersion.getDeckIdentity().toDto());
        export.setDeckVersion(deckVersion.toDto());

        List<DeckEntry> entries = deckEntryRepository.findByDeckVersionIdOrderByIdAsc(deckVersion.getId());
        export.setEntries(entries.stream().map(this::resolveEntry).toList());
        return export;
    }

    private ResolvedDeckEntryDto resolveEntry(DeckEntry entry) {
        ResolvedDeckEntryDto dto = new ResolvedDeckEntryDto();
        dto.setQuantity(entry.getQuantity());
        dto.setCardPrintSet(entry.getCardPrintSet().toDto());
        dto.setCardVersion(entry.getCardPrintSet().getCardVersion().toDto());
        dto.setCardIdentity(entry.getCardPrintSet().getCardVersion().getCardIdentity().toDto());
        dto.setEffectDefinition(entry.getCardPrintSet().getCardVersion().getEffectDefinition().toDto());
        return dto;
    }
}
