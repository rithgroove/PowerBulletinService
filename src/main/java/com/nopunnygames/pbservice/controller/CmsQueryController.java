package com.nopunnygames.pbservice.controller;

import com.nopunnygames.pbservice.service.PowerBulletinCmsQueryService;
import com.nopunnygames.tanuki.core.response.ApiResponse;
import com.nopunnygames.tanuki.core.response.PageMeta;
import com.nopunnygames.tanuki.core.response.PagedResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Explicit read endpoints for the Power Bulletin CMS views.
 */
@RestController
@RequestMapping("/cms")
public class CmsQueryController {
    private final PowerBulletinCmsQueryService queryService;

    /**
     * Creates the CMS query controller.
     *
     * @param queryService CMS query service
     */
    public CmsQueryController(PowerBulletinCmsQueryService queryService) {
        this.queryService = queryService;
    }

    /**
     * Lists card identity summaries.
     *
     * @return card rows
     */
    @GetMapping("/cards")
    public ResponseEntity<ApiResponse<PagedResponse<Map<String, Object>>>> cards(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "") String faction,
            @RequestParam(defaultValue = "") String status,
            @RequestParam(name = "sort_by", defaultValue = "faction") String sortBy,
            @RequestParam(name = "sort_direction", defaultValue = "asc") String sortDirection,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(new ApiResponse<>(200, paged(queryService.listCards(search, faction, status, sortBy, sortDirection), page, limit)));
    }

    /**
     * Reads one card identity for CMS forms.
     *
     * @param cardId card identity UUID
     * @return card row
     */
    @GetMapping("/cards/{cardId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> card(@PathVariable UUID cardId) {
        return ResponseEntity.ok(new ApiResponse<>(200, queryService.card(cardId)));
    }

    /**
     * Lists card version summaries for one card identity.
     *
     * @param cardId card identity UUID
     * @return card version rows
     */
    @GetMapping("/cards/{cardId}/versions")
    public ResponseEntity<ApiResponse<PagedResponse<Map<String, Object>>>> cardVersions(
            @PathVariable UUID cardId,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "") String cardType,
            @RequestParam(defaultValue = "") String status,
            @RequestParam(name = "sort_by", defaultValue = "version") String sortBy,
            @RequestParam(name = "sort_direction", defaultValue = "asc") String sortDirection,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(new ApiResponse<>(200, paged(queryService.listCardVersions(cardId, search, cardType, status, sortBy, sortDirection), page, limit)));
    }

    /**
     * Lists print sets for one card version.
     *
     * @param cardVersionId card version UUID
     * @return print set rows
     */
    @GetMapping("/card-versions/{cardVersionId}/print-sets")
    public ResponseEntity<ApiResponse<PagedResponse<Map<String, Object>>>> cardPrintSets(
            @PathVariable UUID cardVersionId,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "") String status,
            @RequestParam(name = "sort_by", defaultValue = "code") String sortBy,
            @RequestParam(name = "sort_direction", defaultValue = "asc") String sortDirection,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(new ApiResponse<>(200, paged(queryService.listCardPrintSetsByVersion(cardVersionId, search, status, sortBy, sortDirection), page, limit)));
    }

    /**
     * Lists all card print set options.
     *
     * @return print set option rows
     */
    @GetMapping("/card-print-sets/options")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> cardPrintSetOptions(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "") String faction,
            @RequestParam(defaultValue = "") String cardType
    ) {
        return ResponseEntity.ok(new ApiResponse<>(200, queryService.listCardPrintSetOptions(search, faction, cardType)));
    }

    /**
     * Lists deck identity summaries.
     *
     * @return deck rows
     */
    @GetMapping("/decks")
    public ResponseEntity<ApiResponse<PagedResponse<Map<String, Object>>>> decks(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "") String status,
            @RequestParam(name = "sort_by", defaultValue = "code") String sortBy,
            @RequestParam(name = "sort_direction", defaultValue = "asc") String sortDirection,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(new ApiResponse<>(200, paged(queryService.listDecks(search, status, sortBy, sortDirection), page, limit)));
    }

    /**
     * Reads one deck identity for CMS forms.
     *
     * @param deckId deck identity UUID
     * @return deck row
     */
    @GetMapping("/decks/{deckId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deck(@PathVariable UUID deckId) {
        return ResponseEntity.ok(new ApiResponse<>(200, queryService.deck(deckId)));
    }

    /**
     * Lists deck versions for one deck identity.
     *
     * @param deckId deck identity UUID
     * @return deck version rows
     */
    @GetMapping("/decks/{deckId}/versions")
    public ResponseEntity<ApiResponse<PagedResponse<Map<String, Object>>>> deckVersions(
            @PathVariable UUID deckId,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "") String status,
            @RequestParam(name = "sort_by", defaultValue = "version") String sortBy,
            @RequestParam(name = "sort_direction", defaultValue = "asc") String sortDirection,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(new ApiResponse<>(200, paged(queryService.listDeckVersions(deckId, search, status, sortBy, sortDirection), page, limit)));
    }

    /**
     * Lists entries for one deck version.
     *
     * @param deckVersionId deck version UUID
     * @return deck entry rows
     */
    @GetMapping("/deck-versions/{deckVersionId}/entries")
    public ResponseEntity<ApiResponse<PagedResponse<Map<String, Object>>>> deckEntries(
            @PathVariable UUID deckVersionId,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "") String faction,
            @RequestParam(defaultValue = "") String cardType,
            @RequestParam(name = "sort_by", defaultValue = "faction") String sortBy,
            @RequestParam(name = "sort_direction", defaultValue = "asc") String sortDirection,
            @RequestParam(name = "is_paginate", defaultValue = "true") boolean isPaginate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(new ApiResponse<>(200, paged(queryService.listDeckEntries(deckVersionId, search, faction, cardType, sortBy, sortDirection), page, limit, isPaginate)));
    }

    /**
     * Reads one deck version for CMS forms.
     *
     * @param deckVersionId deck version UUID
     * @return deck version row
     */
    @GetMapping("/deck-versions/{deckVersionId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deckVersion(@PathVariable UUID deckVersionId) {
        return ResponseEntity.ok(new ApiResponse<>(200, queryService.deckVersion(deckVersionId)));
    }

    /**
     * Reads one card version for CMS forms.
     *
     * @param cardVersionId card version UUID
     * @return card version row
     */
    @GetMapping("/card-versions/{cardVersionId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> cardVersion(@PathVariable UUID cardVersionId) {
        return ResponseEntity.ok(new ApiResponse<>(200, queryService.cardVersion(cardVersionId)));
    }

    /**
     * Lists simulation run filter options.
     *
     * @return filter options
     */
    @GetMapping("/simulation-runs/filter-options")
    public ResponseEntity<ApiResponse<Map<String, Object>>> simulationRunFilterOptions() {
        return ResponseEntity.ok(new ApiResponse<>(200, queryService.simulationRunFilterOptions()));
    }

    /**
     * Lists simulation runs.
     *
     * @param sortBy sort key
     * @param sortDirection sort direction
     * @param deck deck identity filter
     * @param players player count filter
     * @return simulation run rows
     */
    @GetMapping("/simulation-runs")
    public ResponseEntity<ApiResponse<PagedResponse<Map<String, Object>>>> simulationRuns(
            @RequestParam(name = "sort_by", defaultValue = "created_at") String sortBy,
            @RequestParam(name = "sort_direction", defaultValue = "desc") String sortDirection,
            @RequestParam(defaultValue = "") String deck,
            @RequestParam(defaultValue = "") String players,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(new ApiResponse<>(200, paged(queryService.listSimulationRuns(
                queryService.resultSortKey(sortBy),
                sortDirection,
                deck,
                queryService.playerCount(players),
                search
        ), page, limit)));
    }

    /**
     * Deletes one persisted simulation run.
     *
     * @param runId simulation run UUID
     * @return deletion summary
     */
    @DeleteMapping("/simulation-runs/{runId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteSimulationRun(@PathVariable UUID runId) {
        return ResponseEntity.ok(new ApiResponse<>(200, queryService.deleteSimulationRun(runId)));
    }

    /**
     * Reads one simulation run and its metric summaries.
     *
     * @param runId simulation run UUID
     * @return simulation run detail
     */
    @GetMapping("/simulation-runs/{runId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> simulationRun(@PathVariable UUID runId) {
        return ResponseEntity.ok(new ApiResponse<>(200, queryService.simulationRunDetail(runId)));
    }

    /**
     * Lists grouped simulation runs.
     *
     * @return grouped run rows
     */
    @GetMapping("/simulation-run-groups")
    public ResponseEntity<ApiResponse<PagedResponse<Map<String, Object>>>> simulationRunGroups(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(new ApiResponse<>(200, paged(queryService.listSimulationRunGroups(), page, limit)));
    }

    /**
     * Deletes one grouped simulation run and its child subruns.
     *
     * @param groupId grouped run UUID
     * @return deletion summary
     */
    @DeleteMapping("/simulation-run-groups/{groupId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteSimulationRunGroup(@PathVariable UUID groupId) {
        return ResponseEntity.ok(new ApiResponse<>(200, queryService.deleteSimulationRunGroup(groupId)));
    }

    /**
     * Reads one grouped simulation run.
     *
     * @param groupId grouped run UUID
     * @return grouped run detail
     */
    @GetMapping("/simulation-run-groups/{groupId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> simulationRunGroup(@PathVariable UUID groupId) {
        return ResponseEntity.ok(new ApiResponse<>(200, queryService.simulationRunGroupDetail(groupId)));
    }

    /**
     * Reads grouped run summary JSON.
     *
     * @param groupId grouped run UUID
     * @return grouped summary
     */
    @GetMapping("/simulation-run-groups/{groupId}/summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> simulationRunGroupSummary(@PathVariable UUID groupId) {
        return ResponseEntity.ok(new ApiResponse<>(200, queryService.simulationRunGroupSummary(groupId)));
    }

    /**
     * Lists grouped run subruns.
     *
     * @param groupId grouped run UUID
     * @return subrun rows
     */
    @GetMapping("/simulation-run-groups/{groupId}/subruns")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> simulationRunGroupSubruns(@PathVariable UUID groupId) {
        return ResponseEntity.ok(new ApiResponse<>(200, queryService.simulationRunGroupSubruns(groupId)));
    }

    private PagedResponse<Map<String, Object>> paged(List<Map<String, Object>> rows, int page, int limit) {
        return paged(rows, page, limit, true);
    }

    private PagedResponse<Map<String, Object>> paged(List<Map<String, Object>> rows, int page, int limit, boolean isPaginate) {
        if (!isPaginate) {
            return new PagedResponse<>(rows, new PageMeta(1, rows.size(), rows.size(), 1));
        }
        int safeLimit = Math.max(1, Math.min(100, limit));
        int totalPages = Math.max(1, (int) Math.ceil((double) rows.size() / safeLimit));
        int safePage = Math.max(1, Math.min(page, totalPages));
        int from = Math.min(rows.size(), (safePage - 1) * safeLimit);
        int to = Math.min(rows.size(), from + safeLimit);
        return new PagedResponse<>(rows.subList(from, to), new PageMeta(safePage, safeLimit, rows.size(), totalPages));
    }
}
