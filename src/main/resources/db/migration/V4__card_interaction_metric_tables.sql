CREATE TABLE IF NOT EXISTS card_pair_metrics (
    run_id UUID NOT NULL REFERENCES simulation_runs(id),
    card_a_code TEXT NOT NULL,
    card_b_code TEXT NOT NULL,
    games_with_both_drawn INTEGER NOT NULL DEFAULT 0,
    games_with_both_played INTEGER NOT NULL DEFAULT 0,
    same_player_had_both_count INTEGER NOT NULL DEFAULT 0,
    same_player_had_both_rate DOUBLE PRECISION NOT NULL DEFAULT 0,
    same_end_hand_count INTEGER NOT NULL DEFAULT 0,
    same_end_hand_rate DOUBLE PRECISION NOT NULL DEFAULT 0,
    same_turn_window_count INTEGER NOT NULL DEFAULT 0,
    co_occurrence_rate DOUBLE PRECISION NOT NULL DEFAULT 0,
    sample_size INTEGER NOT NULL DEFAULT 0,
    PRIMARY KEY (run_id, card_a_code, card_b_code)
);

CREATE TABLE IF NOT EXISTS card_sequence_metrics (
    run_id UUID NOT NULL REFERENCES simulation_runs(id),
    source_card_code TEXT NOT NULL,
    followup_card_code TEXT NOT NULL,
    sequence_type TEXT NOT NULL,
    sequence_count INTEGER NOT NULL DEFAULT 0,
    same_turn_count INTEGER NOT NULL DEFAULT 0,
    average_turn_gap DOUBLE PRECISION,
    same_player_sequence_count INTEGER NOT NULL DEFAULT 0,
    opponent_sequence_count INTEGER NOT NULL DEFAULT 0,
    PRIMARY KEY (run_id, source_card_code, followup_card_code, sequence_type)
);

CREATE TABLE IF NOT EXISTS card_counter_metrics (
    run_id UUID NOT NULL REFERENCES simulation_runs(id),
    counter_card_code TEXT NOT NULL,
    countered_card_code TEXT NOT NULL,
    counter_type TEXT NOT NULL,
    counter_count INTEGER NOT NULL DEFAULT 0,
    counter_success_count INTEGER NOT NULL DEFAULT 0,
    counter_success_rate DOUBLE PRECISION NOT NULL DEFAULT 0,
    prevented_discards_estimate DOUBLE PRECISION,
    prevented_draws_estimate DOUBLE PRECISION,
    prevented_net_card_advantage_estimate DOUBLE PRECISION,
    PRIMARY KEY (run_id, counter_card_code, countered_card_code, counter_type)
);

CREATE TABLE IF NOT EXISTS reaction_interaction_metrics (
    run_id UUID NOT NULL REFERENCES simulation_runs(id),
    reaction_card_code TEXT NOT NULL,
    pending_effect_card_code TEXT NOT NULL,
    reaction_count INTEGER NOT NULL DEFAULT 0,
    reaction_success_count INTEGER NOT NULL DEFAULT 0,
    reaction_success_rate DOUBLE PRECISION NOT NULL DEFAULT 0,
    targeted_reaction BOOLEAN,
    targeted_reaction_count INTEGER,
    reacting_player_was_target BOOLEAN,
    reacting_player_was_target_count INTEGER,
    average_turn_reacted DOUBLE PRECISION,
    PRIMARY KEY (run_id, reaction_card_code, pending_effect_card_code)
);

CREATE TABLE IF NOT EXISTS on_discard_chain_metrics (
    run_id UUID NOT NULL REFERENCES simulation_runs(id),
    discard_source_card_code TEXT NOT NULL,
    discarded_card_code TEXT NOT NULL,
    triggered_card_code TEXT NOT NULL,
    chain_count INTEGER NOT NULL DEFAULT 0,
    average_chain_depth DOUBLE PRECISION NOT NULL DEFAULT 0,
    cards_drawn_from_chain INTEGER NOT NULL DEFAULT 0,
    cards_discarded_from_chain INTEGER NOT NULL DEFAULT 0,
    draw_skipped_from_chain_count INTEGER NOT NULL DEFAULT 0,
    PRIMARY KEY (run_id, discard_source_card_code, discarded_card_code, triggered_card_code)
);

CREATE TABLE IF NOT EXISTS power_pressure_interaction_metrics (
    run_id UUID NOT NULL REFERENCES simulation_runs(id),
    source_card_code TEXT NOT NULL,
    removed_card_code TEXT NOT NULL,
    removed_card_power INTEGER,
    removed_card_faction TEXT,
    removed_card_type TEXT,
    was_high_power_card BOOLEAN NOT NULL DEFAULT FALSE,
    target_was_power_leader BOOLEAN,
    power_gap_before DOUBLE PRECISION,
    power_gap_after DOUBLE PRECISION,
    power_gap_delta DOUBLE PRECISION,
    removal_count INTEGER NOT NULL DEFAULT 0,
    removed_count INTEGER,
    average_removed_power DOUBLE PRECISION,
    PRIMARY KEY (
        run_id,
        source_card_code,
        removed_card_code,
        removed_card_power,
        removed_card_faction,
        removed_card_type,
        was_high_power_card
    )
);

CREATE TABLE IF NOT EXISTS card_interaction_summary_metrics (
    run_id UUID NOT NULL REFERENCES simulation_runs(id),
    card_code TEXT NOT NULL,
    interaction_degree INTEGER NOT NULL DEFAULT 0,
    synergy_degree INTEGER NOT NULL DEFAULT 0,
    counter_degree INTEGER NOT NULL DEFAULT 0,
    trigger_degree INTEGER NOT NULL DEFAULT 0,
    reaction_degree INTEGER NOT NULL DEFAULT 0,
    average_interaction_strength DOUBLE PRECISION NOT NULL DEFAULT 0,
    interaction_centrality_score DOUBLE PRECISION NOT NULL DEFAULT 0,
    PRIMARY KEY (run_id, card_code)
);

CREATE INDEX IF NOT EXISTS idx_card_pair_metrics_run_id ON card_pair_metrics(run_id);
CREATE INDEX IF NOT EXISTS idx_card_sequence_metrics_run_id ON card_sequence_metrics(run_id);
CREATE INDEX IF NOT EXISTS idx_card_counter_metrics_run_id ON card_counter_metrics(run_id);
CREATE INDEX IF NOT EXISTS idx_reaction_interaction_metrics_run_id ON reaction_interaction_metrics(run_id);
CREATE INDEX IF NOT EXISTS idx_on_discard_chain_metrics_run_id ON on_discard_chain_metrics(run_id);
CREATE INDEX IF NOT EXISTS idx_power_pressure_interaction_metrics_run_id ON power_pressure_interaction_metrics(run_id);
CREATE INDEX IF NOT EXISTS idx_card_interaction_summary_metrics_run_id ON card_interaction_summary_metrics(run_id);
