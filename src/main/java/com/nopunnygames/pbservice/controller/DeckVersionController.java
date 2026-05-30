package com.nopunnygames.pbservice.controller;

import com.nopunnygames.pbservice.dto.DeckVersionDto;
import com.nopunnygames.pbservice.dto.ProductDeckApplicationDto;
import com.nopunnygames.pbservice.entity.DeckVersion;
import com.nopunnygames.pbservice.repository.DeckVersionRepository;
import com.nopunnygames.pbservice.service.DeckVersionService;
import com.nopunnygames.pbservice.service.ProductService;
import com.nopunnygames.tanuki.core.dto.AuthUser;
import com.nopunnygames.tanuki.core.controller.MasterController;
import com.nopunnygames.tanuki.core.exception.ObjectNotFoundException;
import com.nopunnygames.tanuki.core.exception.ValidationErrorException;
import com.nopunnygames.tanuki.core.response.ApiErrorResponse;
import com.nopunnygames.tanuki.core.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for deck versions.
 */
@RestController
@RequestMapping("/deck-versions")
public class DeckVersionController extends MasterController<DeckVersion, UUID, DeckVersionDto> {
    private final DeckVersionService service;
    private final ProductService productService;
    private final DeckVersionRepository repository;

    /**
     * Creates the deck version controller.
     *
     * @param service deck version service
     * @param productService product service
     * @param repository deck version repository
     */
    public DeckVersionController(DeckVersionService service, ProductService productService, DeckVersionRepository repository) {
        this.service = service;
        this.productService = productService;
        this.repository = repository;
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

    /**
     * Applies a product to this deck version and merges matching deck entries.
     *
     * @param deckVersionId deck version UUID
     * @param productId product UUID
     * @param authentication Spring Security authentication
     * @return application summary response
     */
    @PostMapping("/{deckVersionId}/products/{productId}/add")
    public ResponseEntity<ApiResponse<ProductDeckApplicationDto>> addProduct(
            @PathVariable UUID deckVersionId,
            @PathVariable UUID productId,
            Authentication authentication
    ) {
        AuthUser user = getAuthenticatedUser(authentication);
        ResponseEntity<ApiResponse<ProductDeckApplicationDto>> permissionCheck = checkPermission(user, permissionCode("UPDATE"));
        if (permissionCheck != null) {
            return permissionCheck;
        }

        try {
            return ResponseEntity.ok(new ApiResponse<>(200, productService.addProductToDeckVersion(deckVersionId, productId, user)));
        } catch (ValidationErrorException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiErrorResponse<>(400, null, exception.getMessage(), exception.errors));
        }
    }

    @Override
    protected String getFeatureCode() {
        return "DECK_VERSION";
    }
}
