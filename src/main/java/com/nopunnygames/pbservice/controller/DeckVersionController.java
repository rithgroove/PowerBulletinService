package com.nopunnygames.pbservice.controller;

import com.nopunnygames.pbservice.dto.DeckVersionDto;
import com.nopunnygames.pbservice.entity.DeckVersion;
import com.nopunnygames.pbservice.repository.DeckVersionRepository;
import com.nopunnygames.pbservice.service.DeckVersionService;
import com.nopunnygames.tanuki.core.exception.ObjectNotFoundException;
import com.nopunnygames.tanuki.core.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for deck versions.
 */
@RestController
@RequestMapping("/deck-versions")
public class DeckVersionController extends PublicMasterController<DeckVersion, UUID, DeckVersionDto> {
    private final DeckVersionService service;
    private final DeckVersionRepository repository;

    /**
     * Creates the deck version controller.
     *
     * @param service deck version service
     * @param repository deck version repository
     */
    public DeckVersionController(DeckVersionService service, DeckVersionRepository repository) {
        this.service = service;
        this.repository = repository;
        this.featureName = "DECK_VERSION";
    }

    @Override
    protected DeckVersionService getService() {
        return service;
    }

    /**
     * Reads a deck version by stable code.
     *
     * @param code stable code
     * @return deck version response
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<DeckVersionDto>> getByCode(@PathVariable String code) {
        DeckVersion version = repository.findByCode(code)
                .orElseThrow(() -> new ObjectNotFoundException("Deck version " + code + " not found"));
        return ResponseEntity.ok(new ApiResponse<>(200, version.toCompleteDto()));
    }

    /**
     * Lists deck versions for one deck identity.
     *
     * @param deckIdentityId deck identity UUID
     * @return deck version list response
     */
    @GetMapping("/by-deck/{deckIdentityId}")
    public ResponseEntity<ApiResponse<List<DeckVersionDto>>> listByDeck(@PathVariable UUID deckIdentityId) {
        return ResponseEntity.ok(new ApiResponse<>(200, service.listByDeckIdentity(deckIdentityId)));
    }
}
