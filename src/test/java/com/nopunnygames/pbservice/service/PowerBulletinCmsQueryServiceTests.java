package com.nopunnygames.pbservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Focused tests for Power Bulletin simulation result read models.
 */
class PowerBulletinCmsQueryServiceTests {
    private JdbcTemplate jdbcTemplate;
    private PowerBulletinCmsQueryService queryService;
    private UUID runId;

    @BeforeEach
    void setUp() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1");
        jdbcTemplate = new JdbcTemplate(dataSource);
        queryService = new PowerBulletinCmsQueryService(jdbcTemplate);
        runId = UUID.randomUUID();
        createBaseResultTables();
        insertBaseRun();
    }

    @Test
    void simulationRunDetailWorksWithoutInteractionMetricTables() {
        Map<String, Object> detail = queryService.simulationRunDetail(runId);

        assertThat(detail.get("run")).isInstanceOf(Map.class);
        assertThat(detail.get("runGroup")).isEqualTo(Map.of());
        assertThat(detail.get("cardPairMetrics")).isEqualTo(List.of());
        assertThat(detail.get("reactionInteractionMetrics")).isEqualTo(List.of());
        assertThat(detail.get("cardCounterMetrics")).isEqualTo(List.of());
        assertThat(detail.get("onDiscardChainMetrics")).isEqualTo(List.of());
        assertThat(detail.get("powerPressureInteractionMetrics")).isEqualTo(List.of());
        assertThat(detail.get("cardInteractionSummaryMetrics")).isEqualTo(List.of());
        assertThat(detail.get("cardSequenceMetrics")).isEqualTo(List.of());
        assertThat(detail.get("card_pair_metrics")).isEqualTo(List.of());
        assertThat(detail.get("reaction_interaction_metrics")).isEqualTo(List.of());
        assertThat(detail.get("card_counter_metrics")).isEqualTo(List.of());
        assertThat(detail.get("on_discard_chain_metrics")).isEqualTo(List.of());
        assertThat(detail.get("power_pressure_interaction_metrics")).isEqualTo(List.of());
        assertThat(detail.get("card_interaction_summary_metrics")).isEqualTo(List.of());
        assertThat(detail.get("card_sequence_metrics")).isEqualTo(List.of());
    }

    @Test
    void simulationRunDetailIncludesTurnCountDistributionWhenGameSummariesExist() {
        jdbcTemplate.execute("CREATE TABLE game_summaries (run_id UUID, total_turns INTEGER)");
        jdbcTemplate.update("INSERT INTO game_summaries (run_id, total_turns) VALUES (?, 5)", runId);
        jdbcTemplate.update("INSERT INTO game_summaries (run_id, total_turns) VALUES (?, 5)", runId);
        jdbcTemplate.update("INSERT INTO game_summaries (run_id, total_turns) VALUES (?, 6)", runId);

        Map<String, Object> detail = queryService.simulationRunDetail(runId);

        List<Map<String, Object>> distribution = rows(detail, "turnCountDistribution");
        assertThat(distribution).hasSize(2);
        assertThat(distribution.get(0)).containsEntry("total_turns", 5).containsEntry("game_count", 2L);
        assertThat(distribution.get(1)).containsEntry("total_turns", 6).containsEntry("game_count", 1L);
    }

    @Test
    void simulationRunDetailIncludesPlayerOutcomeRatesWhenGameSummariesExist() {
        jdbcTemplate.execute("""
                CREATE TABLE game_summaries (
                    run_id UUID,
                    winner_player_index INTEGER,
                    deck_out BOOLEAN,
                    final_hand_powers JSON
                )
                """);
        jdbcTemplate.update("INSERT INTO game_summaries (run_id, winner_player_index, deck_out, final_hand_powers) VALUES (?, 0, false, '[8, 4, 3, 2]')", runId);
        jdbcTemplate.update("INSERT INTO game_summaries (run_id, winner_player_index, deck_out, final_hand_powers) VALUES (?, 1, false, '[1, 7, 3, 2]')", runId);
        jdbcTemplate.update("INSERT INTO game_summaries (run_id, winner_player_index, deck_out, final_hand_powers) VALUES (?, null, true, '[5, 5, 1, 0]')", runId);
        jdbcTemplate.update("INSERT INTO game_summaries (run_id, winner_player_index, deck_out, final_hand_powers) VALUES (?, null, true, '[2, 3, 4, 4]')", runId);

        Map<String, Object> detail = queryService.simulationRunDetail(runId);

        List<Map<String, Object>> outcomes = rows(detail, "playerOutcomeRates");
        assertThat(outcomes).hasSize(4);
        assertThat(outcomes.get(0))
                .containsEntry("player_label", "Player 1")
                .containsEntry("game_count", 4)
                .containsEntry("win_count", 1)
                .containsEntry("tie_count", 1)
                .containsEntry("loss_count", 2);
        assertThat(outcomes.get(0).get("win_rate")).isEqualTo(0.25);
        assertThat(outcomes.get(0).get("tie_rate")).isEqualTo(0.25);
        assertThat(outcomes.get(1).get("win_count")).isEqualTo(1);
        assertThat(outcomes.get(1).get("tie_count")).isEqualTo(1);
        assertThat(outcomes.get(2).get("tie_count")).isEqualTo(1);
        assertThat(outcomes.get(3).get("tie_count")).isEqualTo(1);
        assertThat(detail.get("player_outcome_rates")).isSameAs(outcomes);
    }

    @Test
    void simulationRunDetailIncludesInteractionMetricsWhenPresent() {
        createInteractionTables();
        jdbcTemplate.update("""
                INSERT INTO reaction_interaction_metrics (
                    run_id, reaction_card_code, pending_effect_card_code, reaction_count,
                    reaction_success_count, reaction_success_rate, targeted_reaction,
                    reacting_player_was_target, average_turn_reacted
                ) VALUES (?, 'VILLAIN_TACTICIAN_V1', 'UNKNOWN_FUTURE_CARD_V1', 3, 3, 1.25, false, false, 4.5)
                """, runId);
        jdbcTemplate.update("""
                INSERT INTO card_counter_metrics (
                    run_id, counter_card_code, countered_card_code, counter_type, counter_count,
                    counter_success_count, counter_success_rate, prevented_discards_estimate,
                    prevented_draws_estimate, prevented_net_card_advantage_estimate
                ) VALUES (?, 'VILLAIN_TACTICIAN_V1', 'UNDERDOG_V1', 'NEGATE_EFFECT', 2, 2, 1.0, null, null, null)
                """, runId);
        jdbcTemplate.update("""
                INSERT INTO card_interaction_summary_metrics (
                    run_id, card_code, interaction_degree, synergy_degree, counter_degree,
                    trigger_degree, reaction_degree, average_interaction_strength,
                    interaction_centrality_score
                ) VALUES (?, 'NECROMANCER_V1', 5, 0, 3, 0, 2, 1.4, 9.5)
                """, runId);

        Map<String, Object> detail = queryService.simulationRunDetail(runId);

        List<Map<String, Object>> reactions = rows(detail, "reactionInteractionMetrics");
        assertThat(reactions).hasSize(1);
        assertThat(reactions.getFirst().get("pending_effect_card_code")).isEqualTo("UNKNOWN_FUTURE_CARD_V1");
        assertThat(reactions.getFirst().get("reaction_success_rate")).isEqualTo(1.25);
        assertThat(detail.get("reaction_interaction_metrics")).isSameAs(reactions);

        List<Map<String, Object>> counters = rows(detail, "cardCounterMetrics");
        assertThat(counters).hasSize(1);
        assertThat(counters.getFirst().get("countered_card_code")).isEqualTo("UNDERDOG_V1");
        assertThat(counters.getFirst().get("prevented_discards_estimate")).isNull();
        assertThat(counters.getFirst().get("prevented_draws_estimate")).isNull();
        assertThat(counters.getFirst().get("prevented_net_card_advantage_estimate")).isNull();
        assertThat(detail.get("card_counter_metrics")).isSameAs(counters);

        List<Map<String, Object>> summary = rows(detail, "cardInteractionSummaryMetrics");
        assertThat(summary).hasSize(1);
        assertThat(summary.getFirst().get("card_code")).isEqualTo("NECROMANCER_V1");
        assertThat(summary.getFirst().get("interaction_centrality_score")).isEqualTo(9.5);
        assertThat(detail.get("card_interaction_summary_metrics")).isSameAs(summary);
    }

    @Test
    void simulationRunDetailReadsSimulatorNativePowerPressureRows() {
        jdbcTemplate.execute("""
                CREATE TABLE power_pressure_interaction_metrics (
                    run_id UUID,
                    source_card_code TEXT,
                    removed_card_code TEXT,
                    removed_card_power INTEGER,
                    removed_card_faction TEXT,
                    removed_card_type TEXT,
                    was_high_power_card BOOLEAN,
                    removal_count INTEGER
                )
                """);
        jdbcTemplate.update("""
                INSERT INTO power_pressure_interaction_metrics (
                    run_id, source_card_code, removed_card_code, removed_card_power,
                    removed_card_faction, removed_card_type, was_high_power_card, removal_count
                ) VALUES (?, 'VILLAIN_ATTACKER_V1', 'HERO_HITMAN_V1', 6, 'HERO', 'ACTION', true, 7)
                """, runId);

        Map<String, Object> detail = queryService.simulationRunDetail(runId);

        List<Map<String, Object>> rows = rows(detail, "powerPressureInteractionMetrics");
        assertThat(rows).hasSize(1);
        assertThat(rows.getFirst().get("removal_count")).isEqualTo(7);
        assertThat(detail.get("power_pressure_interaction_metrics")).isSameAs(rows);
    }

    @Test
    void deckEntryListIncludesCardIdentityIdsForCmsForms() {
        UUID deckVersionId = UUID.randomUUID();
        UUID cardIdentityId = UUID.randomUUID();
        UUID cardVersionId = UUID.randomUUID();
        UUID printSetId = UUID.randomUUID();
        UUID entryId = UUID.randomUUID();
        createDeckEntryTables();
        jdbcTemplate.update("""
                INSERT INTO card_identities (id, name, faction, deleted_at)
                VALUES (?, 'ATTACKER', 'HERO', null)
                """, cardIdentityId);
        jdbcTemplate.update("""
                INSERT INTO card_versions (id, card_identity_id, version_name, card_type, deleted_at)
                VALUES (?, ?, 'v1', 'ACTION', null)
                """, cardVersionId, cardIdentityId);
        jdbcTemplate.update("""
                INSERT INTO card_print_sets (id, card_version_id, code, deleted_at)
                VALUES (?, ?, 'HERO_ATTACKER_V1_STANDARD', null)
                """, printSetId, cardVersionId);
        jdbcTemplate.update("""
                INSERT INTO deck_entries (id, deck_version_id, card_print_set_id, quantity, deleted_at)
                VALUES (?, ?, ?, 1, null)
                """, entryId, deckVersionId, printSetId);

        List<Map<String, Object>> entries = queryService.listDeckEntries(
                deckVersionId,
                "",
                "",
                "",
                "faction",
                "asc"
        );

        assertThat(entries).hasSize(1);
        assertThat(entries.getFirst())
                .containsEntry("card_identity_id", cardIdentityId)
                .containsEntry("cardIdentityId", cardIdentityId);
    }

    @Test
    void groupedRunListAndDetailReturnMetadataSubrunsAndSummary() {
        UUID groupId = UUID.randomUUID();
        createGroupedRunTables();
        jdbcTemplate.update("""
                INSERT INTO simulation_run_groups (
                    id, run_group_code, run_group_name, deck_code, deck_name, version_name,
                    rng_seed, requested_iterations_per_player_count, player_counts, created_at, notes
                ) VALUES (?, 'GROUP_A', 'Aurora-Miho-Caffe', 'TEST_DECK_V1', 'Test Deck', 'v1', 1234, 10, '[2,3,4]', ?, null)
                """, groupId, Timestamp.from(Instant.parse("2026-05-31T00:00:00Z")));
        jdbcTemplate.update("""
                INSERT INTO simulation_run_group_members (
                    id, run_group_id, run_id, run_code, player_count, total_games, rng_seed, status, created_at
                ) VALUES (?, ?, ?, 'RUN_A', 4, 10, 42, 'COMPLETED', ?)
                """, UUID.randomUUID(), groupId, runId, Timestamp.from(Instant.parse("2026-05-31T00:00:00Z")));
        jdbcTemplate.update("""
                INSERT INTO simulation_run_group_summaries (id, run_group_id, summary_json, created_at)
                VALUES (?, ?, '{"cross_player_summary":{"deck_out_rate_range":0.2}}', ?)
                """, UUID.randomUUID(), groupId, Timestamp.from(Instant.parse("2026-05-31T00:00:00Z")));

        assertThat(queryService.listSimulationRunGroups("created_at", "desc", "", "")).hasSize(1);
        Map<String, Object> detail = queryService.simulationRunGroupDetail(groupId);
        assertThat(map(detail, "group")).containsEntry("run_group_code", "GROUP_A");
        assertThat((List<?>) detail.get("subruns")).hasSize(1);
        assertThat(map(detail, "summary")).containsKey("summary_json");
        assertThat(queryService.simulationRunDetail(runId).get("runGroup")).isInstanceOf(Map.class);
    }

    @Test
    void groupedRunDetailDoesNotCrashWhenSummaryIsMissing() {
        UUID groupId = UUID.randomUUID();
        createGroupedRunTables();
        jdbcTemplate.update("""
                INSERT INTO simulation_run_groups (
                    id, run_group_code, run_group_name, deck_code, requested_iterations_per_player_count,
                    player_counts, created_at
                ) VALUES (?, 'GROUP_EMPTY', 'Aurora-Miho', 'TEST_DECK_V1', 10, '[2,3,4]', ?)
                """, groupId, Timestamp.from(Instant.parse("2026-05-31T00:00:00Z")));

        Map<String, Object> detail = queryService.simulationRunGroupDetail(groupId);

        assertThat(map(detail, "group")).containsEntry("run_group_code", "GROUP_EMPTY");
        assertThat(detail.get("subruns")).isEqualTo(List.of());
        assertThat(detail.get("summary")).isEqualTo(Map.of());
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> rows(Map<String, Object> detail, String key) {
        return (List<Map<String, Object>>) detail.get(key);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> map(Map<String, Object> detail, String key) {
        return (Map<String, Object>) detail.get(key);
    }

    private void createBaseResultTables() {
        jdbcTemplate.execute("""
                CREATE TABLE deck_identities (
                    id UUID PRIMARY KEY,
                    name TEXT NOT NULL
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE deck_versions (
                    id UUID PRIMARY KEY,
                    deck_identity_id UUID NOT NULL,
                    code TEXT NOT NULL,
                    version_name TEXT NOT NULL
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE simulation_runs (
                    id UUID PRIMARY KEY,
                    run_code TEXT NOT NULL,
                    deck_version_id UUID NOT NULL,
                    total_games INTEGER NOT NULL,
                    player_count INTEGER NOT NULL,
                    rng_seed BIGINT NOT NULL,
                    created_at TIMESTAMP NOT NULL,
                    notes TEXT
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE run_metric_summaries (
                    run_id UUID PRIMARY KEY,
                    deck_out_rate DOUBLE PRECISION,
                    last_man_standing_rate DOUBLE PRECISION,
                    elimination_rate DOUBLE PRECISION,
                    average_turns DOUBLE PRECISION,
                    median_turns DOUBLE PRECISION,
                    average_final_hand_size DOUBLE PRECISION,
                    average_final_hand_power DOUBLE PRECISION,
                    average_cards_drawn_per_game DOUBLE PRECISION,
                    average_cards_discarded_per_game DOUBLE PRECISION,
                    average_reactions_per_game DOUBLE PRECISION
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE advanced_run_metric_summaries (
                    run_id UUID PRIMARY KEY,
                    low_hand_pressure_rate DOUBLE PRECISION,
                    average_peak_tension_turn DOUBLE PRECISION,
                    memorability_score DOUBLE PRECISION,
                    pressure_fairness_score DOUBLE PRECISION
                )
                """);
        jdbcTemplate.execute("CREATE TABLE turn_curve_metric_summaries (run_id UUID, turn_number INTEGER)");
        jdbcTemplate.execute("CREATE TABLE card_metric_summaries (run_id UUID, card_version_code TEXT, played_win_rate DOUBLE PRECISION, end_hand_win_rate DOUBLE PRECISION, end_hand_player_count INTEGER, end_hand_player_win_count INTEGER)");
        jdbcTemplate.execute("CREATE TABLE card_gravity_metric_summaries (run_id UUID, card_version_code TEXT, card_gravity_score DOUBLE PRECISION)");
        jdbcTemplate.execute("CREATE TABLE effect_metric_summaries (run_id UUID, effect_code TEXT, effect_resolve_count INTEGER)");
    }

    private void insertBaseRun() {
        UUID deckIdentityId = UUID.randomUUID();
        UUID deckVersionId = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO deck_identities (id, name) VALUES (?, 'Test Deck')", deckIdentityId);
        jdbcTemplate.update("""
                INSERT INTO deck_versions (id, deck_identity_id, code, version_name)
                VALUES (?, ?, 'TEST_DECK_V1', 'v1')
                """, deckVersionId, deckIdentityId);
        jdbcTemplate.update("""
                INSERT INTO simulation_runs (
                    id, run_code, deck_version_id, total_games, player_count, rng_seed, created_at, notes
                ) VALUES (?, 'RUN_A', ?, 10, 4, 42, ?, null)
                """, runId, deckVersionId, Timestamp.from(Instant.parse("2026-05-31T00:00:00Z")));
        jdbcTemplate.update("""
                INSERT INTO run_metric_summaries (
                    run_id, deck_out_rate, last_man_standing_rate, elimination_rate,
                    average_turns, median_turns, average_final_hand_size,
                    average_final_hand_power, average_cards_drawn_per_game,
                    average_cards_discarded_per_game, average_reactions_per_game
                ) VALUES (?, 0.5, 0.5, 0.1, 10.0, 10.0, 2.0, 8.0, 5.0, 4.0, 1.0)
                """, runId);
        jdbcTemplate.update("""
                INSERT INTO advanced_run_metric_summaries (
                    run_id, low_hand_pressure_rate, average_peak_tension_turn,
                    memorability_score, pressure_fairness_score
                ) VALUES (?, 0.2, 6.0, 1.5, 0.8)
                """, runId);
    }

    private void createInteractionTables() {
        jdbcTemplate.execute("""
                CREATE TABLE reaction_interaction_metrics (
                    run_id UUID,
                    reaction_card_code TEXT,
                    pending_effect_card_code TEXT,
                    reaction_count INTEGER,
                    reaction_success_count INTEGER,
                    reaction_success_rate DOUBLE PRECISION,
                    targeted_reaction BOOLEAN,
                    reacting_player_was_target BOOLEAN,
                    average_turn_reacted DOUBLE PRECISION
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE card_counter_metrics (
                    run_id UUID,
                    counter_card_code TEXT,
                    countered_card_code TEXT,
                    counter_type TEXT,
                    counter_count INTEGER,
                    counter_success_count INTEGER,
                    counter_success_rate DOUBLE PRECISION,
                    prevented_discards_estimate DOUBLE PRECISION,
                    prevented_draws_estimate DOUBLE PRECISION,
                    prevented_net_card_advantage_estimate DOUBLE PRECISION
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE card_interaction_summary_metrics (
                    run_id UUID,
                    card_code TEXT,
                    interaction_degree INTEGER,
                    synergy_degree INTEGER,
                    counter_degree INTEGER,
                    trigger_degree INTEGER,
                    reaction_degree INTEGER,
                    average_interaction_strength DOUBLE PRECISION,
                    interaction_centrality_score DOUBLE PRECISION
                )
                """);
    }

    private void createGroupedRunTables() {
        jdbcTemplate.execute("""
                CREATE TABLE simulation_run_groups (
                    id UUID PRIMARY KEY,
                    run_group_code TEXT,
                    run_group_name TEXT,
                    deck_code TEXT,
                    deck_name TEXT,
                    version_name TEXT,
                    rng_seed BIGINT,
                    requested_iterations_per_player_count INTEGER,
                    player_counts TEXT,
                    created_at TIMESTAMP,
                    notes TEXT
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE simulation_run_group_members (
                    id UUID PRIMARY KEY,
                    run_group_id UUID,
                    run_id UUID,
                    run_code TEXT,
                    player_count INTEGER,
                    total_games INTEGER,
                    rng_seed BIGINT,
                    status TEXT,
                    created_at TIMESTAMP
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE simulation_run_group_summaries (
                    id UUID PRIMARY KEY,
                    run_group_id UUID,
                    summary_json TEXT,
                    created_at TIMESTAMP
                )
                """);
    }

    private void createDeckEntryTables() {
        jdbcTemplate.execute("""
                CREATE TABLE card_identities (
                    id UUID PRIMARY KEY,
                    name TEXT NOT NULL,
                    faction TEXT NOT NULL,
                    deleted_at TIMESTAMP
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE card_versions (
                    id UUID PRIMARY KEY,
                    card_identity_id UUID NOT NULL,
                    version_name TEXT NOT NULL,
                    card_type TEXT NOT NULL,
                    deleted_at TIMESTAMP
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE card_print_sets (
                    id UUID PRIMARY KEY,
                    card_version_id UUID NOT NULL,
                    code TEXT NOT NULL,
                    deleted_at TIMESTAMP
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE card_print_set_powers (
                    card_print_set_id UUID,
                    power INTEGER,
                    power_order INTEGER
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE deck_entries (
                    id UUID PRIMARY KEY,
                    deck_version_id UUID NOT NULL,
                    card_print_set_id UUID NOT NULL,
                    quantity INTEGER NOT NULL,
                    deleted_at TIMESTAMP
                )
                """);
    }
}
