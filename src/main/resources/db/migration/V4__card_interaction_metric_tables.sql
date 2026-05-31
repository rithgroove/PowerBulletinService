CREATE TABLE IF NOT EXISTS card_pair_metrics (
    id BIGSERIAL PRIMARY KEY,
    run_id UUID NOT NULL REFERENCES simulation_runs(id),
    card_a_code TEXT,
    card_b_code TEXT,
    games_with_both_drawn INTEGER,
    games_with_both_played INTEGER,
    same_player_had_both_count INTEGER,
    same_player_had_both_rate DOUBLE PRECISION,
    same_end_hand_count INTEGER,
    same_end_hand_rate DOUBLE PRECISION,
    same_turn_window_count INTEGER,
    co_occurrence_rate DOUBLE PRECISION,
    sample_size INTEGER
);

CREATE INDEX IF NOT EXISTS idx_card_pair_metrics_run_id ON card_pair_metrics(run_id);

CREATE TABLE IF NOT EXISTS card_sequence_metrics (
    id BIGSERIAL PRIMARY KEY,
    run_id UUID NOT NULL REFERENCES simulation_runs(id),
    source_card_code TEXT,
    followup_card_code TEXT,
    sequence_type TEXT,
    sequence_count INTEGER,
    same_turn_count INTEGER,
    average_turn_gap DOUBLE PRECISION,
    same_player_sequence_count INTEGER,
    opponent_sequence_count INTEGER
);

CREATE INDEX IF NOT EXISTS idx_card_sequence_metrics_run_id ON card_sequence_metrics(run_id);

CREATE TABLE IF NOT EXISTS card_counter_metrics (
    id BIGSERIAL PRIMARY KEY,
    run_id UUID NOT NULL REFERENCES simulation_runs(id),
    counter_card_code TEXT,
    countered_card_code TEXT,
    counter_type TEXT,
    counter_count INTEGER,
    counter_success_count INTEGER,
    counter_success_rate DOUBLE PRECISION,
    prevented_discards_estimate DOUBLE PRECISION,
    prevented_draws_estimate DOUBLE PRECISION,
    prevented_net_card_advantage_estimate DOUBLE PRECISION
);

CREATE INDEX IF NOT EXISTS idx_card_counter_metrics_run_id ON card_counter_metrics(run_id);

CREATE TABLE IF NOT EXISTS reaction_interaction_metrics (
    id BIGSERIAL PRIMARY KEY,
    run_id UUID NOT NULL REFERENCES simulation_runs(id),
    reaction_card_code TEXT,
    pending_effect_card_code TEXT,
    reaction_count INTEGER,
    reaction_success_count INTEGER,
    reaction_success_rate DOUBLE PRECISION,
    targeted_reaction BOOLEAN,
    reacting_player_was_target BOOLEAN,
    average_turn_reacted DOUBLE PRECISION
);

CREATE INDEX IF NOT EXISTS idx_reaction_interaction_metrics_run_id ON reaction_interaction_metrics(run_id);

CREATE TABLE IF NOT EXISTS on_discard_chain_metrics (
    id BIGSERIAL PRIMARY KEY,
    run_id UUID NOT NULL REFERENCES simulation_runs(id),
    discard_source_card_code TEXT,
    discarded_card_code TEXT,
    triggered_card_code TEXT,
    chain_count INTEGER,
    average_chain_depth DOUBLE PRECISION,
    cards_drawn_from_chain INTEGER,
    cards_discarded_from_chain INTEGER,
    draw_skipped_from_chain_count INTEGER
);

CREATE INDEX IF NOT EXISTS idx_on_discard_chain_metrics_run_id ON on_discard_chain_metrics(run_id);

CREATE TABLE IF NOT EXISTS power_pressure_interaction_metrics (
    id BIGSERIAL PRIMARY KEY,
    run_id UUID NOT NULL REFERENCES simulation_runs(id),
    source_card_code TEXT,
    removed_card_code TEXT,
    removed_card_power DOUBLE PRECISION,
    removed_card_faction TEXT,
    removed_card_type TEXT,
    was_high_power_card BOOLEAN,
    target_was_power_leader BOOLEAN,
    power_gap_before DOUBLE PRECISION,
    power_gap_after DOUBLE PRECISION,
    power_gap_delta DOUBLE PRECISION,
    removed_count INTEGER,
    average_removed_power DOUBLE PRECISION
);

CREATE INDEX IF NOT EXISTS idx_power_pressure_interaction_metrics_run_id ON power_pressure_interaction_metrics(run_id);

CREATE TABLE IF NOT EXISTS card_interaction_summary_metrics (
    id BIGSERIAL PRIMARY KEY,
    run_id UUID NOT NULL REFERENCES simulation_runs(id),
    card_code TEXT,
    interaction_degree INTEGER,
    synergy_degree INTEGER,
    counter_degree INTEGER,
    trigger_degree INTEGER,
    reaction_degree INTEGER,
    average_interaction_strength DOUBLE PRECISION,
    interaction_centrality_score DOUBLE PRECISION
);

CREATE INDEX IF NOT EXISTS idx_card_interaction_summary_metrics_run_id ON card_interaction_summary_metrics(run_id);
