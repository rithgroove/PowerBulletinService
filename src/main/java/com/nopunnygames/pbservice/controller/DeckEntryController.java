package com.nopunnygames.pbservice.controller;

import com.nopunnygames.pbservice.dto.DeckEntryDto;
import com.nopunnygames.pbservice.entity.DeckEntry;
import com.nopunnygames.pbservice.service.DeckEntryService;
import com.nopunnygames.tanuki.core.controller.BaseController;
import com.nopunnygames.tanuki.core.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for deck entries.
 */
@RestController
@RequestMapping("/deck-entries")
public class DeckEntryController extends BaseController<DeckEntry, UUID, DeckEntryDto> {
    private final DeckEntryService service;

    /**
     * Creates the deck entry controller.
     *
     * @param service deck entry service
     */
    public DeckEntryController(DeckEntryService service) {
        this.service = service;
    }

    @Override
    protected DeckEntryService getService() {
        return service;
    }

    /**
     * Lists deck entries for one deck version.
     *
     * @param deckVersionId deck version UUID
     * @return deck entry list response
     */
    @GetMapping("/by-version/{deckVersionId}")
    public ResponseEntity<ApiResponse<List<DeckEntryDto>>> listByDeckVersion(@PathVariable UUID deckVersionId) {
        return ResponseEntity.ok(new ApiResponse<>(200, service.listByDeckVersion(deckVersionId)));
    }

    @Override
    protected String getFeatureCode() {
        return "DECK_ENTRY";
    }
}
