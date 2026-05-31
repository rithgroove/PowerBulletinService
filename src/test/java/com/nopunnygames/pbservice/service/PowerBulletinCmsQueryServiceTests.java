package com.nopunnygames.pbservice.service;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PowerBulletinCmsQueryServiceTests {
    @Test
    void simulationRunDetailReturnsEmptyInteractionMetricsWhenTablesAreAbsent() {
        JdbcTemplate jdbcTemplate = jdbcTemplate();
        createBaseSimulationSchema(jdbcTemplate);
        UUID runId = seedBaseRun(jdbcTemplate);

        Map<String, Object> detail = new PowerBulletinCmsQueryService(jdbcTemplate).simulationRunDetail(runId);

        assertThat(detail.get("card_pair_metrics")).isEqualTo(List.of());
        assertThat(detail.get("reaction_interaction_metrics")).isEqualTo(List.of());
        assertThat(detail.get("card_counter_metrics")).isEqualTo(List.of());
        assertThat(detail.get("on_discard_chain_metrics")).isEqualTo(List.of());
        assertThat(detail.get("power_pressure_interaction_metrics")).isEqualTo(List.of());
        assertThat(detail.get("card_interaction_summary_metrics")).isEqualTo(List.of());
        assertThat(detail.get("card_sequence_metrics")).isEqualTo(List.of());
    }

    @Test
    void simulationRunDetailIncludesInteractionMetricsAndPreservesNullsAndLargeRates() {
        JdbcTemplate jdbcTemplate = jdbcTemplate();
        createBaseSimulationSchema(jdbcTemplate);
        createInteractionMetricSchema(jdbcTemplate);
        UUID runId = seedBaseRun(jdbcTemplate);
        jdbcTemplate.update("""
                INSERT INTO reaction_interaction_metrics (
                    run_id, reaction_card_code, pending_effect_card_code, reaction_count,
                    reaction_success_count, reaction_success_rate, targeted_reaction,
                    reacting_player_was_target, average_turn_reacted
                )
                VALUES (?, 'FUTURE_REACTION_V1', 'UNKNOWN_ACTION_V1', 4, 3, 1.25, TRUE, FALSE, 2.5)
                """, runId);
        jdbcTemplate.update("""
                INSERT INTO card_counter_metrics (
                    run_id, counter_card_code, countered_card_code, counter_type, counter_count,
                    counter_success_count, counter_success_rate, prevented_discards_estimate,
                    prevented_draws_estimate, prevented_net_card_advantage_estimate
                )
                VALUES (?, 'HERO_GUARD_V1', 'UNDERDOG_V1', 'NEGATE', 3, 2, 0.667, NULL, NULL, NULL)
                """, runId);
        jdbcTemplate.update("""
                INSERT INTO card_interaction_summary_metrics (
                    run_id, card_code, interaction_degree, synergy_degree, counter_degree,
                    trigger_degree, reaction_degree, average_interaction_strength,
                    interaction_centrality_score
                )
                VALUES (?, 'NECROMANCER_V1', 7, 2, 1, 3, 1, 1.75, 2.4)
                """, runId);

        Map<String, Object> detail = new PowerBulletinCmsQueryService(jdbcTemplate).simulationRunDetail(runId);

        List<Map<String, Object>> reactions = metricRows(detail, "reaction_interaction_metrics");
        assertThat(reactions).hasSize(1);
        assertThat(reactions.getFirst().get("reaction_card_code")).isEqualTo("FUTURE_REACTION_V1");
        assertThat(reactions.getFirst().get("reaction_success_rate")).isEqualTo(1.25);

        List<Map<String, Object>> counters = metricRows(detail, "card_counter_metrics");
        assertThat(counters).hasSize(1);
        assertThat(counters.getFirst().get("prevented_discards_estimate")).isNull();
        assertThat(counters.getFirst().get("prevented_draws_estimate")).isNull();
        assertThat(counters.getFirst().get("prevented_net_card_advantage_estimate")).isNull();

        List<Map<String, Object>> summaries = metricRows(detail, "card_interaction_summary_metrics");
        assertThat(summaries).hasSize(1);
        assertThat(summaries.getFirst().get("card_code")).isEqualTo("NECROMANCER_V1");
        assertThat(summaries.getFirst().get("interaction_centrality_score")).isEqualTo(2.4);
    }

    private JdbcTemplate jdbcTemplate() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1");
        dataSource.setUsername("sa");
        return new JdbcTemplate(dataSource);
    }

    private void createBaseSimulationSchema(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("CREATE TABLE deck_identities (id UUID PRIMARY KEY, name TEXT)");
        jdbcTemplate.execute("CREATE TABLE deck_versions (id UUID PRIMARY KEY, deck_identity_id UUID, code TEXT, version_name TEXT)");
        jdbcTemplate.execute("""
                CREATE TABLE simulation_runs (
                    id UUID PRIMARY KEY, run_code TEXT, deck_version_id UUID, total_games INTEGER,
                    player_count INTEGER, rng_seed BIGINT, created_at TIMESTAMP, notes TEXT
                )
                """);
        jdbcTemplate.execute("CREATE TABLE run_metric_summaries (run_id UUID PRIMARY KEY)");
        jdbcTemplate.execute("CREATE TABLE advanced_run_metric_summaries (run_id UUID PRIMARY KEY)");
        jdbcTemplate.execute("CREATE TABLE turn_curve_metric_summaries (run_id UUID, turn_number INTEGER)");
        jdbcTemplate.execute("CREATE TABLE card_metric_summaries (run_id UUID, card_version_code TEXT, played_win_rate DOUBLE PRECISION)");
        jdbcTemplate.execute("CREATE TABLE card_gravity_metric_summaries (run_id UUID, card_version_code TEXT, card_gravity_score DOUBLE PRECISION)");
        jdbcTemplate.execute("CREATE TABLE effect_metric_summaries (run_id UUID, effect_code TEXT, effect_resolve_count INTEGER)");
    }

    private UUID seedBaseRun(JdbcTemplate jdbcTemplate) {
        UUID deckIdentityId = UUID.randomUUID();
        UUID deckVersionId = UUID.randomUUID();
        UUID runId = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO deck_identities (id, name) VALUES (?, 'Standard')", deckIdentityId);
        jdbcTemplate.update("INSERT INTO deck_versions (id, deck_identity_id, code, version_name) VALUES (?, ?, 'STANDARD_POWER_BULLETIN', 'v1')", deckVersionId, deckIdentityId);
        jdbcTemplate.update("""
                INSERT INTO simulation_runs (
                    id, run_code, deck_version_id, total_games, player_count, rng_seed, created_at, notes
                )
                VALUES (?, 'test-run', ?, 10, 2, 1, CURRENT_TIMESTAMP, NULL)
                """, runId, deckVersionId);
        return runId;
    }

    private void createInteractionMetricSchema(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("""
                CREATE TABLE card_pair_metrics (
                    run_id UUID, card_a_code TEXT, card_b_code TEXT, games_with_both_drawn INTEGER,
                    games_with_both_played INTEGER, same_player_had_both_count INTEGER,
                    same_player_had_both_rate DOUBLE PRECISION, same_end_hand_count INTEGER,
                    same_end_hand_rate DOUBLE PRECISION, same_turn_window_count INTEGER,
                    co_occurrence_rate DOUBLE PRECISION, sample_size INTEGER
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE card_sequence_metrics (
                    run_id UUID, source_card_code TEXT, followup_card_code TEXT, sequence_type TEXT,
                    sequence_count INTEGER, same_turn_count INTEGER, average_turn_gap DOUBLE PRECISION,
                    same_player_sequence_count INTEGER, opponent_sequence_count INTEGER
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE card_counter_metrics (
                    run_id UUID, counter_card_code TEXT, countered_card_code TEXT, counter_type TEXT,
                    counter_count INTEGER, counter_success_count INTEGER, counter_success_rate DOUBLE PRECISION,
                    prevented_discards_estimate DOUBLE PRECISION, prevented_draws_estimate DOUBLE PRECISION,
                    prevented_net_card_advantage_estimate DOUBLE PRECISION
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE reaction_interaction_metrics (
                    run_id UUID, reaction_card_code TEXT, pending_effect_card_code TEXT,
                    reaction_count INTEGER, reaction_success_count INTEGER, reaction_success_rate DOUBLE PRECISION,
                    targeted_reaction BOOLEAN, reacting_player_was_target BOOLEAN, average_turn_reacted DOUBLE PRECISION
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE on_discard_chain_metrics (
                    run_id UUID, discard_source_card_code TEXT, discarded_card_code TEXT, triggered_card_code TEXT,
                    chain_count INTEGER, average_chain_depth DOUBLE PRECISION, cards_drawn_from_chain INTEGER,
                    cards_discarded_from_chain INTEGER, draw_skipped_from_chain_count INTEGER
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE power_pressure_interaction_metrics (
                    run_id UUID, source_card_code TEXT, removed_card_code TEXT, removed_card_power DOUBLE PRECISION,
                    removed_card_faction TEXT, removed_card_type TEXT, was_high_power_card BOOLEAN,
                    target_was_power_leader BOOLEAN, power_gap_before DOUBLE PRECISION, power_gap_after DOUBLE PRECISION,
                    power_gap_delta DOUBLE PRECISION, removed_count INTEGER, average_removed_power DOUBLE PRECISION
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE card_interaction_summary_metrics (
                    run_id UUID, card_code TEXT, interaction_degree INTEGER, synergy_degree INTEGER,
                    counter_degree INTEGER, trigger_degree INTEGER, reaction_degree INTEGER,
                    average_interaction_strength DOUBLE PRECISION, interaction_centrality_score DOUBLE PRECISION
                )
                """);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> metricRows(Map<String, Object> detail, String key) {
        return (List<Map<String, Object>>) detail.get(key);
    }
}
