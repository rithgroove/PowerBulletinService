package com.nopunnygames.pbservice.controller;

import com.nopunnygames.pbservice.dto.DeckIdentityDto;
import com.nopunnygames.pbservice.entity.DeckIdentity;
import com.nopunnygames.pbservice.repository.DeckIdentityRepository;
import com.nopunnygames.pbservice.service.DeckIdentityService;
import com.nopunnygames.tanuki.core.exception.ObjectNotFoundException;
import com.nopunnygames.tanuki.core.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST controller for deck identities.
 */
@RestController
@RequestMapping("/decks")
public class DeckIdentityController extends PublicMasterController<DeckIdentity, UUID, DeckIdentityDto> {
    private final DeckIdentityService service;
    private final DeckIdentityRepository repository;

    /**
     * Creates the deck identity controller.
     *
     * @param service deck identity service
     * @param repository deck identity repository
     */
    public DeckIdentityController(DeckIdentityService service, DeckIdentityRepository repository) {
        this.service = service;
        this.repository = repository;
        this.featureName = "DECK_IDENTITY";
    }

    @Override
    protected DeckIdentityService getService() {
        return service;
    }

    /**
     * Reads a deck identity by stable code.
     *
     * @param code stable code
     * @return deck identity response
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<DeckIdentityDto>> getByCode(@PathVariable String code) {
        DeckIdentity deck = repository.findByCode(code)
                .orElseThrow(() -> new ObjectNotFoundException("Deck identity " + code + " not found"));
        return ResponseEntity.ok(new ApiResponse<>(200, deck.toCompleteDto()));
    }
}
