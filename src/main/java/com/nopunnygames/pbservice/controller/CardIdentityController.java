package com.nopunnygames.pbservice.controller;

import com.nopunnygames.pbservice.dto.CardIdentityDto;
import com.nopunnygames.pbservice.entity.CardIdentity;
import com.nopunnygames.pbservice.repository.CardIdentityRepository;
import com.nopunnygames.pbservice.service.CardIdentityService;
import com.nopunnygames.tanuki.core.exception.ObjectNotFoundException;
import com.nopunnygames.tanuki.core.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST controller for card identities.
 */
@RestController
@RequestMapping("/cards")
public class CardIdentityController extends PublicMasterController<CardIdentity, UUID, CardIdentityDto> {
    private final CardIdentityService service;
    private final CardIdentityRepository repository;

    /**
     * Creates the card identity controller.
     *
     * @param service card identity service
     * @param repository card identity repository
     */
    public CardIdentityController(CardIdentityService service, CardIdentityRepository repository) {
        this.service = service;
        this.repository = repository;
        this.featureName = "CARD_IDENTITY";
    }

    @Override
    protected CardIdentityService getService() {
        return service;
    }

    /**
     * Reads a card identity by stable code.
     *
     * @param code stable code
     * @return card identity response
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<CardIdentityDto>> getByCode(@PathVariable String code) {
        CardIdentity card = repository.findByCode(code)
                .orElseThrow(() -> new ObjectNotFoundException("Card identity " + code + " not found"));
        return ResponseEntity.ok(new ApiResponse<>(200, card.toCompleteDto()));
    }
}
