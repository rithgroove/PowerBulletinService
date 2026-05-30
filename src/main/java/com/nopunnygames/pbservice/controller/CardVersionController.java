package com.nopunnygames.pbservice.controller;

import com.nopunnygames.pbservice.dto.CardVersionDto;
import com.nopunnygames.pbservice.entity.CardVersion;
import com.nopunnygames.pbservice.repository.CardVersionRepository;
import com.nopunnygames.pbservice.service.CardVersionService;
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
 * REST controller for card versions.
 */
@RestController
@RequestMapping("/card-versions")
public class CardVersionController extends MasterController<CardVersion, UUID, CardVersionDto> {
    private final CardVersionService service;
    private final CardVersionRepository repository;

    /**
     * Creates the card version controller.
     *
     * @param service card version service
     * @param repository card version repository
     */
    public CardVersionController(CardVersionService service, CardVersionRepository repository) {
        this.service = service;
        this.repository = repository;
    }

    @Override
    protected CardVersionService getService() {
        return service;
    }

    /**
     * Reads a card version by stable code.
     *
     * @param code stable code
     * @return card version response
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<CardVersionDto>> getByCode(@PathVariable String code) {
        CardVersion version = repository.findByCode(code)
                .orElseThrow(() -> new ObjectNotFoundException("Card version " + code + " not found"));
        return ResponseEntity.ok(new ApiResponse<>(200, version.toCompleteDto()));
    }

    /**
     * Lists versions for one card identity.
     *
     * @param cardIdentityId card identity UUID
     * @return card version list response
     */
    @GetMapping("/by-card/{cardIdentityId}")
    public ResponseEntity<ApiResponse<List<CardVersionDto>>> listByCard(@PathVariable UUID cardIdentityId) {
        return ResponseEntity.ok(new ApiResponse<>(200, service.listByCardIdentity(cardIdentityId)));
    }

    @Override
    protected String getFeatureCode() {
        return "CARD_VERSION";
    }
}
