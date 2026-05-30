package com.nopunnygames.pbservice.controller;

import com.nopunnygames.pbservice.dto.CardIdentityDto;
import com.nopunnygames.pbservice.dto.DeckExportDto;
import com.nopunnygames.pbservice.dto.ProductExportDto;
import com.nopunnygames.pbservice.service.PowerBulletinExportService;
import com.nopunnygames.tanuki.core.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for resolved simulator export endpoints.
 */
@RestController
@RequestMapping("/exports")
public class ExportController {
    private final PowerBulletinExportService service;

    /**
     * Creates the export controller.
     *
     * @param service export service
     */
    public ExportController(PowerBulletinExportService service) {
        this.service = service;
    }

    /**
     * Returns the full card tree.
     *
     * @return complete card tree response
     */
    @GetMapping("/cards/tree")
    public ResponseEntity<ApiResponse<List<CardIdentityDto>>> cardTree() {
        return ResponseEntity.ok(new ApiResponse<>(200, service.getCardTree()));
    }

    /**
     * Returns a resolved deck export by deck version UUID.
     *
     * @param deckVersionId deck version UUID
     * @return deck export response
     */
    @GetMapping("/deck-versions/{deckVersionId}")
    public ResponseEntity<ApiResponse<DeckExportDto>> deckExport(@PathVariable UUID deckVersionId) {
        return ResponseEntity.ok(new ApiResponse<>(200, service.getDeckExport(deckVersionId)));
    }

    /**
     * Returns a resolved deck export by deck version code.
     *
     * @param code deck version code
     * @return deck export response
     */
    @GetMapping("/deck-versions/code/{code}")
    public ResponseEntity<ApiResponse<DeckExportDto>> deckExportByCode(@PathVariable String code) {
        return ResponseEntity.ok(new ApiResponse<>(200, service.getDeckExportByCode(code)));
    }

    /**
     * Returns the standard base-game deck export.
     *
     * @return standard deck export response
     */
    @GetMapping("/base-game")
    public ResponseEntity<ApiResponse<DeckExportDto>> baseGameExport() {
        return ResponseEntity.ok(new ApiResponse<>(200, service.getDeckExportByCode("STANDARD_POWER_BULLETIN_V0_0")));
    }

    /**
     * Returns active products and their print set quantities.
     *
     * @return product export response
     */
    @GetMapping("/products")
    public ResponseEntity<ApiResponse<ProductExportDto>> productExport() {
        return ResponseEntity.ok(new ApiResponse<>(200, service.getProductExport()));
    }
}
