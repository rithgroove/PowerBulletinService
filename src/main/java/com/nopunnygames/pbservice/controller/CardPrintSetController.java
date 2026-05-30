package com.nopunnygames.pbservice.controller;

import com.nopunnygames.pbservice.dto.CardPrintSetDto;
import com.nopunnygames.pbservice.entity.CardPrintSet;
import com.nopunnygames.pbservice.repository.CardPrintSetRepository;
import com.nopunnygames.pbservice.service.CardPrintSetService;
import com.nopunnygames.tanuki.core.controller.MasterController;
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
 * REST controller for card print sets.
 */
@RestController
@RequestMapping("/card-print-sets")
public class CardPrintSetController extends MasterController<CardPrintSet, UUID, CardPrintSetDto> {
    private final CardPrintSetService service;
    private final CardPrintSetRepository repository;

    /**
     * Creates the card print set controller.
     *
     * @param service card print set service
     * @param repository card print set repository
     */
    public CardPrintSetController(CardPrintSetService service, CardPrintSetRepository repository) {
        this.service = service;
        this.repository = repository;
    }

    @Override
    protected CardPrintSetService getService() {
        return service;
    }

    /**
     * Reads a print set by stable code.
     *
     * @param code stable code
     * @return print set response
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<CardPrintSetDto>> getByCode(@PathVariable String code) {
        CardPrintSet printSet = repository.findByCode(code)
                .orElseThrow(() -> new ObjectNotFoundException("Card print set " + code + " not found"));
        return ResponseEntity.ok(new ApiResponse<>(200, printSet.toCompleteDto()));
    }

    /**
     * Lists print sets for one card version.
     *
     * @param cardVersionId card version UUID
     * @return print set list response
     */
    @GetMapping("/by-version/{cardVersionId}")
    public ResponseEntity<ApiResponse<List<CardPrintSetDto>>> listByVersion(@PathVariable UUID cardVersionId) {
        return ResponseEntity.ok(new ApiResponse<>(200, service.listByCardVersion(cardVersionId)));
    }

    @Override
    protected String getFeatureCode() {
        return "CARD_PRINT_SET";
    }
}
