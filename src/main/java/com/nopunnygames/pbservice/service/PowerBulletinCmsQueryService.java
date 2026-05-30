package com.nopunnygames.pbservice.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Read queries used by the server-rendered Power Bulletin admin screens.
 */
@Service
public class PowerBulletinCmsQueryService {
    private static final Map<String, String> CARD_SORT_COLUMNS = Map.ofEntries(
            Map.entry("code", "LOWER(ci.code)"),
            Map.entry("name", "LOWER(ci.name)"),
            Map.entry("character", "LOWER(ci.character_name)"),
            Map.entry("faction", "LOWER(ci.faction)"),
            Map.entry("versions", "COUNT(DISTINCT cv.id)"),
            Map.entry("print_sets", "COUNT(DISTINCT cps.id)"),
            Map.entry("status", "LOWER(ci.status)")
    );
    private static final Map<String, String> CARD_VERSION_SORT_COLUMNS = Map.ofEntries(
            Map.entry("code", "LOWER(cv.code)"),
            Map.entry("version", "LOWER(cv.version_name)"),
            Map.entry("type", "LOWER(cv.card_type)"),
            Map.entry("effect", "LOWER(ed.code)"),
            Map.entry("print_sets", "COUNT(cps.id)"),
            Map.entry("status", "LOWER(cv.status)")
    );
    private static final Map<String, String> PRINT_SET_SORT_COLUMNS = Map.ofEntries(
            Map.entry("code", "LOWER(cps.code)"),
            Map.entry("powers", "COALESCE(string_agg(cpsp.power::text, ', ' ORDER BY cpsp.power_order), '')"),
            Map.entry("status", "LOWER(cps.status)")
    );
    private static final Map<String, String> DECK_SORT_COLUMNS = Map.ofEntries(
            Map.entry("code", "LOWER(di.code)"),
            Map.entry("name", "LOWER(di.name)"),
            Map.entry("versions", "COUNT(dv.id)"),
            Map.entry("status", "LOWER(di.status)")
    );
    private static final Map<String, String> DECK_VERSION_SORT_COLUMNS = Map.ofEntries(
            Map.entry("code", "LOWER(dv.code)"),
            Map.entry("version", "LOWER(dv.version_name)"),
            Map.entry("entries", "COUNT(de.id)"),
            Map.entry("status", "LOWER(dv.status)")
    );
    private static final Map<String, String> DECK_ENTRY_SORT_COLUMNS = Map.ofEntries(
            Map.entry("card", "LOWER(ci.name)"),
            Map.entry("print_set", "LOWER(cps.code)"),
            Map.entry("faction", "LOWER(ci.faction)"),
            Map.entry("type", "LOWER(cv.card_type)"),
            Map.entry("powers", "COALESCE(string_agg(cpsp.power::text, ', ' ORDER BY cpsp.power_order), '')"),
            Map.entry("quantity", "de.quantity")
    );
    private static final Map<String, String> RESULT_SORT_COLUMNS = Map.ofEntries(
            Map.entry("deck", "LOWER(di.name)"),
            Map.entry("version", "LOWER(dv.version_name)"),
            Map.entry("games", "sr.total_games"),
            Map.entry("players", "sr.player_count"),
            Map.entry("deck_out", "rms.deck_out_rate"),
            Map.entry("last_standing", "rms.last_man_standing_rate"),
            Map.entry("elimination", "rms.elimination_rate"),
            Map.entry("agency", "(rms.agency_metrics ->> 'agency_score')::numeric"),
            Map.entry("comeback", "(rms.comeback_metrics ->> 'comeback_win_rate')::numeric"),
            Map.entry("deck_left", "(rms.pacing_metrics ->> 'average_deck_remaining')::numeric"),
            Map.entry("average_turns", "rms.average_turns"),
            Map.entry("low_hand_pressure", "arms.low_hand_pressure_rate"),
            Map.entry("peak_tension", "arms.average_peak_tension_turn"),
            Map.entry("memorability", "arms.memorability_score"),
            Map.entry("created_at", "sr.created_at")
    );

    private final JdbcTemplate jdbcTemplate;

    /**
     * Creates the CMS query service.
     *
     * @param jdbcTemplate JDBC template
     */
    public PowerBulletinCmsQueryService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Lists card identity summaries.
     *
     * @return card rows
     */
    public List<Map<String, Object>> listCards(String search, String faction, String status, String sortBy, String direction) {
        List<Object> params = new ArrayList<>();
        StringBuilder where = new StringBuilder("WHERE ci.deleted_at IS NULL");
        addSearch(where, params, search, "ci.code", "ci.name", "ci.character_name", "ci.faction");
        addEquals(where, params, "ci.faction", faction);
        addEquals(where, params, "ci.status", status);
        String sql = """
                SELECT
                    ci.id,
                    ci.code,
                    ci.name,
                    ci.character_name,
                    ci.faction,
                    ci.status,
                    COUNT(DISTINCT cv.id) AS version_count,
                    COUNT(DISTINCT cps.id) AS print_set_count
                FROM card_identities ci
                LEFT JOIN card_versions cv ON cv.card_identity_id = ci.id AND cv.deleted_at IS NULL
                LEFT JOIN card_print_sets cps ON cps.card_version_id = cv.id AND cps.deleted_at IS NULL
                %s
                GROUP BY ci.id
                ORDER BY %s
                """.formatted(where, orderBy(CARD_SORT_COLUMNS, sortBy, direction, "faction", "LOWER(ci.name) ASC, LOWER(ci.code) ASC"));
        return jdbcTemplate.queryForList(sql, params.toArray());
    }

    /**
     * Reads one card identity for CMS forms.
     *
     * @param cardId card identity UUID
     * @return card row
     */
    public Map<String, Object> card(UUID cardId) {
        return jdbcTemplate.queryForMap("""
                SELECT
                    ci.id,
                    ci.code,
                    ci.name,
                    ci.character_name,
                    ci.character_name AS "characterName",
                    ci.faction,
                    ci.notes,
                    ci.status
                FROM card_identities ci
                WHERE ci.id = ? AND ci.deleted_at IS NULL
                """, cardId);
    }

    /**
     * Lists card version summaries for one card.
     *
     * @param cardId card identity UUID
     * @return card version rows
     */
    public List<Map<String, Object>> listCardVersions(UUID cardId, String search, String cardType, String status, String sortBy, String direction) {
        List<Object> params = new ArrayList<>();
        StringBuilder where = new StringBuilder("WHERE cv.deleted_at IS NULL AND cv.card_identity_id = ?");
        params.add(cardId);
        addSearch(where, params, search, "cv.code", "cv.version_name", "cv.card_type", "cv.effect_text", "ed.code");
        addEquals(where, params, "cv.card_type", cardType);
        addEquals(where, params, "cv.status", status);
        String sql = """
                SELECT
                    cv.id,
                    cv.code,
                    cv.card_identity_id,
                    ci.name AS card_name,
                    ci.faction,
                    cv.effect_definition_id,
                    ed.code AS effect_code,
                    cv.version_name,
                    cv.card_type,
                    cv.effect_text,
                    cv.status,
                    COUNT(cps.id) AS print_set_count
                FROM card_versions cv
                JOIN card_identities ci ON ci.id = cv.card_identity_id AND ci.deleted_at IS NULL
                JOIN effect_definitions ed ON ed.id = cv.effect_definition_id AND ed.deleted_at IS NULL
                LEFT JOIN card_print_sets cps ON cps.card_version_id = cv.id AND cps.deleted_at IS NULL
                %s
                GROUP BY cv.id, ci.id, ed.id
                ORDER BY %s
                """.formatted(where, orderBy(CARD_VERSION_SORT_COLUMNS, sortBy, direction, "version", "LOWER(cv.code) ASC"));
        return jdbcTemplate.queryForList(sql, params.toArray());
    }

    /**
     * Lists card print set summaries for one card version.
     *
     * @param cardVersionId card version UUID
     * @return print set rows
     */
    public List<Map<String, Object>> listCardPrintSetsByVersion(UUID cardVersionId, String search, String status, String sortBy, String direction) {
        List<Object> params = new ArrayList<>();
        StringBuilder where = new StringBuilder("WHERE cps.deleted_at IS NULL AND cps.card_version_id = ?");
        params.add(cardVersionId);
        addSearch(where, params, search, "cps.code", "cv.code", "ci.name", "cv.version_name");
        addEquals(where, params, "cps.status", status);
        String sql = """
                SELECT
                    cps.id,
                    cps.code,
                    cps.card_version_id,
                    cv.code AS card_version_code,
                    ci.name AS card_name,
                    cv.version_name,
                    cps.status,
                    COALESCE(string_agg(cpsp.power::text, ', ' ORDER BY cpsp.power_order), '') AS powers
                FROM card_print_sets cps
                JOIN card_versions cv ON cv.id = cps.card_version_id AND cv.deleted_at IS NULL
                JOIN card_identities ci ON ci.id = cv.card_identity_id AND ci.deleted_at IS NULL
                LEFT JOIN card_print_set_powers cpsp ON cpsp.card_print_set_id = cps.id
                %s
                GROUP BY cps.id, cv.id, ci.id
                ORDER BY %s
                """.formatted(where, orderBy(PRINT_SET_SORT_COLUMNS, sortBy, direction, "code", ""));
        return jdbcTemplate.queryForList(sql, params.toArray());
    }

    /**
     * Lists all card print set options.
     *
     * @return print set option rows
     */
    public List<Map<String, Object>> listCardPrintSetOptions() {
        return listCardPrintSetOptions("", "", "");
    }

    /**
     * Lists all card print set options with filters.
     *
     * @param search search keyword
     * @param faction optional faction filter
     * @param cardType optional card type filter
     * @return print set option rows
     */
    public List<Map<String, Object>> listCardPrintSetOptions(String search, String faction, String cardType) {
        List<Object> params = new ArrayList<>();
        StringBuilder where = new StringBuilder("WHERE cps.deleted_at IS NULL");
        addSearch(where, params, search, "cps.code", "ci.name", "cv.version_name", "ci.faction", "cv.card_type");
        addEquals(where, params, "ci.faction", faction);
        addEquals(where, params, "cv.card_type", cardType);
        String sql = """
                SELECT
                    cps.id,
                    cps.code,
                    ci.name AS card_name,
                    cv.version_name,
                    ci.faction,
                    cv.card_type,
                    COALESCE(string_agg(cpsp.power::text, ', ' ORDER BY cpsp.power_order), '') AS powers
                FROM card_print_sets cps
                JOIN card_versions cv ON cv.id = cps.card_version_id AND cv.deleted_at IS NULL
                JOIN card_identities ci ON ci.id = cv.card_identity_id AND ci.deleted_at IS NULL
                LEFT JOIN card_print_set_powers cpsp ON cpsp.card_print_set_id = cps.id
                %s
                GROUP BY cps.id, ci.id, cv.id
                ORDER BY ci.faction ASC, ci.name ASC, cv.version_name ASC, cps.code ASC
                """.formatted(where);
        return jdbcTemplate.queryForList(sql, params.toArray());
    }

    /**
     * Lists deck identity summaries.
     *
     * @return deck rows
     */
    public List<Map<String, Object>> listDecks(String search, String status, String sortBy, String direction) {
        List<Object> params = new ArrayList<>();
        StringBuilder where = new StringBuilder("WHERE di.deleted_at IS NULL");
        addSearch(where, params, search, "di.code", "di.name", "di.notes");
        addEquals(where, params, "di.status", status);
        String sql = """
                SELECT
                    di.id,
                    di.code,
                    di.name,
                    di.notes,
                    di.status,
                    COUNT(dv.id) AS version_count
                FROM deck_identities di
                LEFT JOIN deck_versions dv ON dv.deck_identity_id = di.id AND dv.deleted_at IS NULL
                %s
                GROUP BY di.id
                ORDER BY %s
                """.formatted(where, orderBy(DECK_SORT_COLUMNS, sortBy, direction, "code", "LOWER(di.name) ASC"));
        return jdbcTemplate.queryForList(sql, params.toArray());
    }

    /**
     * Reads one deck identity for CMS forms.
     *
     * @param deckId deck identity UUID
     * @return deck row
     */
    public Map<String, Object> deck(UUID deckId) {
        return jdbcTemplate.queryForMap("""
                SELECT
                    di.id,
                    di.code,
                    di.name,
                    di.notes,
                    di.status
                FROM deck_identities di
                WHERE di.id = ? AND di.deleted_at IS NULL
                """, deckId);
    }

    /**
     * Lists deck versions for one deck identity.
     *
     * @param deckId deck identity UUID
     * @return deck version rows
     */
    public List<Map<String, Object>> listDeckVersions(UUID deckId, String search, String status, String sortBy, String direction) {
        List<Object> params = new ArrayList<>();
        StringBuilder where = new StringBuilder("WHERE dv.deleted_at IS NULL AND dv.deck_identity_id = ?");
        params.add(deckId);
        addSearch(where, params, search, "dv.code", "dv.version_name", "dv.notes");
        addEquals(where, params, "dv.status", status);
        String sql = """
                SELECT
                    dv.id,
                    dv.code,
                    dv.deck_identity_id,
                    di.name AS deck_name,
                    dv.version_name,
                    dv.notes,
                    dv.status,
                    COUNT(de.id) AS entry_count
                FROM deck_versions dv
                JOIN deck_identities di ON di.id = dv.deck_identity_id AND di.deleted_at IS NULL
                LEFT JOIN deck_entries de ON de.deck_version_id = dv.id AND de.deleted_at IS NULL
                %s
                GROUP BY dv.id, di.id
                ORDER BY %s
                """.formatted(where, orderBy(DECK_VERSION_SORT_COLUMNS, sortBy, direction, "version", "LOWER(dv.code) ASC"));
        return jdbcTemplate.queryForList(sql, params.toArray());
    }

    /**
     * Reads one deck version for CMS forms.
     *
     * @param deckVersionId deck version UUID
     * @return deck version row
     */
    public Map<String, Object> deckVersion(UUID deckVersionId) {
        return jdbcTemplate.queryForMap("""
                SELECT
                    dv.id,
                    dv.deck_identity_id,
                    dv.deck_identity_id AS "deckIdentityId",
                    dv.code,
                    dv.version_name,
                    dv.version_name AS "versionName",
                    dv.notes,
                    dv.status
                FROM deck_versions dv
                WHERE dv.id = ? AND dv.deleted_at IS NULL
                """, deckVersionId);
    }

    /**
     * Reads one card version for CMS forms.
     *
     * @param cardVersionId card version UUID
     * @return card version row
     */
    public Map<String, Object> cardVersion(UUID cardVersionId) {
        return jdbcTemplate.queryForMap("""
                SELECT
                    cv.id,
                    cv.card_identity_id,
                    cv.card_identity_id AS "cardIdentityId",
                    ci.code AS card_code,
                    ci.code AS "cardCode",
                    ci.name AS card_name,
                    ci.name AS "cardName",
                    ci.character_name AS character_name,
                    ci.character_name AS "characterName",
                    ci.faction,
                    cv.effect_definition_id,
                    cv.effect_definition_id AS "effectDefinitionId",
                    ed.code AS effect_code,
                    ed.code AS "effectCode",
                    cv.code,
                    cv.version_name,
                    cv.version_name AS "versionName",
                    cv.card_type,
                    cv.card_type AS "cardType",
                    cv.effect_text,
                    cv.effect_text AS "effectText",
                    cv.status
                FROM card_versions cv
                JOIN card_identities ci ON ci.id = cv.card_identity_id AND ci.deleted_at IS NULL
                JOIN effect_definitions ed ON ed.id = cv.effect_definition_id AND ed.deleted_at IS NULL
                WHERE cv.id = ? AND cv.deleted_at IS NULL
                """, cardVersionId);
    }

    /**
     * Lists one deck version's entries.
     *
     * @param deckVersionId deck version UUID
     * @return deck entry rows
     */
    public List<Map<String, Object>> listDeckEntries(UUID deckVersionId, String search, String faction, String cardType, String sortBy, String direction) {
        List<Object> params = new ArrayList<>();
        StringBuilder where = new StringBuilder("WHERE de.deck_version_id = ? AND de.deleted_at IS NULL");
        params.add(deckVersionId);
        addSearch(where, params, search, "cps.code", "ci.name", "cv.version_name", "ci.faction", "cv.card_type");
        addEquals(where, params, "ci.faction", faction);
        addEquals(where, params, "cv.card_type", cardType);
        String sql = """
                SELECT
                    de.id,
                    de.card_print_set_id,
                    cps.code AS print_set_code,
                    ci.name AS card_name,
                    cv.version_name,
                    ci.faction,
                    cv.card_type,
                    COALESCE(string_agg(cpsp.power::text, ', ' ORDER BY cpsp.power_order), '') AS powers,
                    de.quantity
                FROM deck_entries de
                JOIN card_print_sets cps ON cps.id = de.card_print_set_id AND cps.deleted_at IS NULL
                JOIN card_versions cv ON cv.id = cps.card_version_id AND cv.deleted_at IS NULL
                JOIN card_identities ci ON ci.id = cv.card_identity_id AND ci.deleted_at IS NULL
                LEFT JOIN card_print_set_powers cpsp ON cpsp.card_print_set_id = cps.id
                %s
                GROUP BY de.id, cps.id, ci.id, cv.id
                ORDER BY %s
                """.formatted(where, orderBy(DECK_ENTRY_SORT_COLUMNS, sortBy, direction, "faction", "LOWER(ci.name) ASC, LOWER(cps.code) ASC"));
        return jdbcTemplate.queryForList(sql, params.toArray());
    }

    /**
     * Lists simulation runs.
     *
     * @param sortBy sort key
     * @param direction sort direction
     * @param deckIdentityId optional deck filter
     * @param playerCount optional player count filter
     * @return simulation run rows
     */
    public List<Map<String, Object>> listSimulationRuns(String sortBy, String direction, String deckIdentityId, Integer playerCount, String search) {
        String normalizedSort = RESULT_SORT_COLUMNS.getOrDefault(sortBy, RESULT_SORT_COLUMNS.get("created_at"));
        String normalizedDirection = "asc".equalsIgnoreCase(direction) ? "ASC" : "DESC";
        StringBuilder where = new StringBuilder("WHERE dv.deleted_at IS NULL AND di.deleted_at IS NULL");
        List<Object> params = new java.util.ArrayList<>();
        if (deckIdentityId != null && !deckIdentityId.isBlank()) {
            where.append(" AND di.id::text = ?");
            params.add(deckIdentityId);
        }
        if (playerCount != null) {
            where.append(" AND sr.player_count = ?");
            params.add(playerCount);
        }
        addSearch(where, params, search, "sr.run_code", "dv.code", "dv.version_name", "di.name", "sr.notes");

        String sql = """
                SELECT
                    sr.id,
                    sr.run_code,
                    dv.code AS deck_code,
                    di.id AS deck_identity_id,
                    di.name AS deck_name,
                    dv.version_name,
                    sr.total_games,
                    sr.player_count,
                    sr.rng_seed,
                    sr.created_at,
                    sr.notes,
                    rms.deck_out_rate,
                    rms.average_turns,
                    rms.last_man_standing_rate,
                    rms.elimination_rate,
                    (rms.agency_metrics ->> 'agency_score')::numeric AS agency_score,
                    (rms.comeback_metrics ->> 'comeback_win_rate')::numeric AS comeback_win_rate,
                    (rms.pacing_metrics ->> 'average_deck_remaining')::numeric AS average_deck_remaining,
                    arms.low_hand_pressure_rate,
                    arms.average_peak_tension_turn,
                    arms.memorability_score
                FROM simulation_runs sr
                JOIN deck_versions dv ON dv.id = sr.deck_version_id
                JOIN deck_identities di ON di.id = dv.deck_identity_id
                LEFT JOIN run_metric_summaries rms ON rms.run_id = sr.id
                LEFT JOIN advanced_run_metric_summaries arms ON arms.run_id = sr.id
                %s
                ORDER BY %s %s NULLS LAST, sr.created_at DESC, sr.run_code DESC
                """.formatted(where, normalizedSort, normalizedDirection);
        return jdbcTemplate.queryForList(sql, params.toArray());
    }

    /**
     * Returns filter options for simulation runs.
     *
     * @return filter options
     */
    public Map<String, Object> simulationRunFilterOptions() {
        List<Map<String, Object>> decks = jdbcTemplate.queryForList("""
                SELECT DISTINCT di.id::text AS value, di.name AS label
                FROM simulation_runs sr
                JOIN deck_versions dv ON dv.id = sr.deck_version_id AND dv.deleted_at IS NULL
                JOIN deck_identities di ON di.id = dv.deck_identity_id AND di.deleted_at IS NULL
                ORDER BY di.name ASC
                """);
        List<Map<String, Object>> players = jdbcTemplate.queryForList("""
                SELECT DISTINCT sr.player_count AS value, sr.player_count::text AS label
                FROM simulation_runs sr
                ORDER BY sr.player_count ASC
                """);
        return Map.of("decks", decks, "players", players);
    }

    /**
     * Returns one simulation run and its metric tables.
     *
     * @param runId simulation run UUID
     * @return detail map
     */
    public Map<String, Object> simulationRunDetail(UUID runId) {
        Map<String, Object> run = jdbcTemplate.queryForMap("""
                SELECT
                    sr.*,
                    dv.code AS deck_code,
                    dv.version_name,
                    di.name AS deck_name
                FROM simulation_runs sr
                JOIN deck_versions dv ON dv.id = sr.deck_version_id
                JOIN deck_identities di ON di.id = dv.deck_identity_id
                WHERE sr.id = ?
                """, runId);
        return Map.of(
                "run", run,
                "metrics", optionalQueryForMap("SELECT * FROM run_metric_summaries WHERE run_id = ?", runId),
                "advancedMetrics", optionalQueryForMap("SELECT * FROM advanced_run_metric_summaries WHERE run_id = ?", runId),
                "turnCurveMetrics", jdbcTemplate.queryForList("SELECT * FROM turn_curve_metric_summaries WHERE run_id = ? ORDER BY turn_number ASC", runId),
                "cardMetrics", jdbcTemplate.queryForList("SELECT * FROM card_metric_summaries WHERE run_id = ? ORDER BY played_win_rate DESC, card_version_code ASC", runId),
                "cardGravityMetrics", jdbcTemplate.queryForList("SELECT * FROM card_gravity_metric_summaries WHERE run_id = ? ORDER BY card_gravity_score DESC, card_version_code ASC", runId),
                "effectMetrics", jdbcTemplate.queryForList("SELECT * FROM effect_metric_summaries WHERE run_id = ? ORDER BY effect_resolve_count DESC, effect_code ASC", runId)
        );
    }

    /**
     * Parses and bounds a player count filter.
     *
     * @param raw raw player count
     * @return parsed player count or null
     */
    public Integer playerCount(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            int value = Integer.parseInt(raw);
            return value >= 2 && value <= 4 ? value : null;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    /**
     * Normalizes a result sort key.
     *
     * @param sortBy raw sort key
     * @return supported sort key
     */
    public String resultSortKey(String sortBy) {
        String normalized = sortBy == null ? "created_at" : sortBy.toLowerCase(Locale.ROOT);
        return RESULT_SORT_COLUMNS.containsKey(normalized) ? normalized : "created_at";
    }

    private String orderBy(Map<String, String> columns, String sortBy, String direction, String defaultSort, String fallback) {
        String normalizedSort = sortBy == null ? defaultSort : sortBy.toLowerCase(Locale.ROOT);
        String column = columns.getOrDefault(normalizedSort, columns.get(defaultSort));
        String normalizedDirection = "desc".equalsIgnoreCase(direction) ? "DESC" : "ASC";
        String suffix = fallback == null || fallback.isBlank() ? "" : ", " + fallback;
        return column + " " + normalizedDirection + " NULLS LAST" + suffix;
    }

    private Map<String, Object> optionalQueryForMap(String sql, UUID id) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, id);
        return rows.isEmpty() ? Map.of() : rows.getFirst();
    }

    private void addSearch(StringBuilder where, List<Object> params, String search, String... columns) {
        if (search == null || search.isBlank() || columns.length == 0) {
            return;
        }
        where.append(" AND (");
        for (int index = 0; index < columns.length; index++) {
            if (index > 0) {
                where.append(" OR ");
            }
            where.append("LOWER(").append(columns[index]).append(") LIKE ?");
            params.add("%" + search.trim().toLowerCase(Locale.ROOT) + "%");
        }
        where.append(")");
    }

    private void addEquals(StringBuilder where, List<Object> params, String column, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        where.append(" AND ").append(column).append(" = ?");
        params.add(value.trim());
    }
}
