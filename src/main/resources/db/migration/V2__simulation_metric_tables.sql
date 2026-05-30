CREATE TABLE IF NOT EXISTS simulation_runs (
    id UUID PRIMARY KEY,
    run_code TEXT NOT NULL UNIQUE,
    deck_version_id UUID NOT NULL REFERENCES deck_versions(id),
    total_games INTEGER NOT NULL,
    player_count INTEGER NOT NULL DEFAULT 2 CHECK (player_count BETWEEN 2 AND 4),
    rng_seed BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    notes TEXT
);

CREATE TABLE IF NOT EXISTS game_summaries (
    game_id UUID PRIMARY KEY,
    run_id UUID NOT NULL REFERENCES simulation_runs(id),
    result_type TEXT NOT NULL,
    winner_player_index INTEGER,
    total_turns INTEGER NOT NULL,
    deck_out BOOLEAN NOT NULL,
    final_hand_powers JSONB NOT NULL,
    cards_drawn INTEGER NOT NULL,
    cards_discarded INTEGER NOT NULL,
    reactions_played INTEGER NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_game_summaries_run_id ON game_summaries(run_id);

ALTER TABLE game_summaries
    ADD COLUMN IF NOT EXISTS final_hand_sizes JSONB NOT NULL DEFAULT '[]'::jsonb,
    ADD COLUMN IF NOT EXISTS cards_drawn_per_player JSONB NOT NULL DEFAULT '[]'::jsonb,
    ADD COLUMN IF NOT EXISTS cards_discarded_per_player JSONB NOT NULL DEFAULT '[]'::jsonb,
    ADD COLUMN IF NOT EXISTS minimum_hand_size_per_player JSONB NOT NULL DEFAULT '[]'::jsonb,
    ADD COLUMN IF NOT EXISTS eliminated_players JSONB NOT NULL DEFAULT '[]'::jsonb,
    ADD COLUMN IF NOT EXISTS elimination_turn_by_player JSONB NOT NULL DEFAULT '[]'::jsonb,
    ADD COLUMN IF NOT EXISTS negation_count INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS targeted_effect_count INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS non_targeted_effect_count INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS reveal_count INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS on_discard_trigger_count INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS deck_remaining_at_end INTEGER DEFAULT 0 NOT NULL,
    ADD COLUMN IF NOT EXISTS turn_count_bucket TEXT DEFAULT 'EARLY_GAME' NOT NULL,
    ADD COLUMN IF NOT EXISTS game_end_phase TEXT DEFAULT 'EARLY_GAME' NOT NULL,
    ADD COLUMN IF NOT EXISTS hand_pressure_metrics JSONB DEFAULT '{}'::jsonb NOT NULL,
    ADD COLUMN IF NOT EXISTS agency_metrics JSONB DEFAULT '{}'::jsonb NOT NULL,
    ADD COLUMN IF NOT EXISTS comeback_metrics JSONB DEFAULT '{}'::jsonb NOT NULL,
    ADD COLUMN IF NOT EXISTS targeting_metrics JSONB DEFAULT '{}'::jsonb NOT NULL,
    ADD COLUMN IF NOT EXISTS average_hand_size_by_turn JSONB DEFAULT '{}'::jsonb NOT NULL;

CREATE TABLE IF NOT EXISTS run_metric_summaries (
    run_id UUID PRIMARY KEY REFERENCES simulation_runs(id),
    deck_out_rate DOUBLE PRECISION NOT NULL,
    last_man_standing_rate DOUBLE PRECISION NOT NULL,
    average_turns DOUBLE PRECISION NOT NULL,
    median_turns DOUBLE PRECISION NOT NULL,
    average_final_hand_size DOUBLE PRECISION NOT NULL,
    average_final_hand_power DOUBLE PRECISION NOT NULL,
    average_cards_discarded_per_game DOUBLE PRECISION NOT NULL,
    average_cards_drawn_per_game DOUBLE PRECISION NOT NULL,
    average_reactions_per_game DOUBLE PRECISION NOT NULL,
    average_cards_drawn_per_player JSONB NOT NULL,
    average_cards_discarded_per_player JSONB NOT NULL,
    average_minimum_hand_size JSONB NOT NULL,
    elimination_rate DOUBLE PRECISION NOT NULL,
    average_elimination_turn DOUBLE PRECISION NOT NULL,
    reaction_rate DOUBLE PRECISION NOT NULL,
    negation_rate DOUBLE PRECISION NOT NULL,
    targeting_rate DOUBLE PRECISION NOT NULL,
    on_discard_trigger_rate DOUBLE PRECISION NOT NULL
);

ALTER TABLE run_metric_summaries
    ADD COLUMN IF NOT EXISTS win_rate_by_player_position JSONB NOT NULL DEFAULT '[]'::jsonb,
    ADD COLUMN IF NOT EXISTS pacing_metrics JSONB DEFAULT '{}'::jsonb NOT NULL,
    ADD COLUMN IF NOT EXISTS hand_pressure_metrics JSONB DEFAULT '{}'::jsonb NOT NULL,
    ADD COLUMN IF NOT EXISTS agency_metrics JSONB DEFAULT '{}'::jsonb NOT NULL,
    ADD COLUMN IF NOT EXISTS comeback_metrics JSONB DEFAULT '{}'::jsonb NOT NULL,
    ADD COLUMN IF NOT EXISTS targeting_metrics JSONB DEFAULT '{}'::jsonb NOT NULL;

CREATE TABLE IF NOT EXISTS card_metric_summaries (
    run_id UUID NOT NULL REFERENCES simulation_runs(id),
    card_version_code TEXT NOT NULL,
    card_play_count INTEGER NOT NULL,
    card_draw_count INTEGER NOT NULL,
    card_discard_count INTEGER NOT NULL,
    card_end_hand_count INTEGER NOT NULL,
    average_power_when_discarded DOUBLE PRECISION NOT NULL,
    average_power_when_kept DOUBLE PRECISION NOT NULL,
    PRIMARY KEY (run_id, card_version_code)
);

ALTER TABLE card_metric_summaries
    ADD COLUMN IF NOT EXISTS starting_hand_player_count INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS starting_hand_player_win_count INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS starting_hand_win_rate DOUBLE PRECISION NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS discarded_player_count INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS discarded_player_win_count INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS discarded_win_rate DOUBLE PRECISION NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS played_player_count INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS played_player_win_count INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS played_win_rate DOUBLE PRECISION NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS deckout_end_hand_player_count INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS deckout_end_hand_player_win_count INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS deckout_end_hand_win_rate DOUBLE PRECISION NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS card_feel_metrics JSONB DEFAULT '{}'::jsonb NOT NULL;

CREATE TABLE IF NOT EXISTS effect_metric_summaries (
    run_id UUID NOT NULL REFERENCES simulation_runs(id),
    effect_code TEXT NOT NULL,
    effect_resolve_count INTEGER NOT NULL,
    effect_negation_count INTEGER NOT NULL,
    average_discards_per_effect_resolution DOUBLE PRECISION NOT NULL,
    average_draws_per_effect_resolution DOUBLE PRECISION NOT NULL,
    PRIMARY KEY (run_id, effect_code)
);

ALTER TABLE effect_metric_summaries
    ADD COLUMN IF NOT EXISTS effect_efficiency_metrics JSONB DEFAULT '{}'::jsonb NOT NULL;

CREATE TABLE IF NOT EXISTS advanced_run_metric_summaries (
    run_id UUID PRIMARY KEY REFERENCES simulation_runs(id),
    tension_curve_by_turn JSONB NOT NULL DEFAULT '{}'::jsonb,
    average_peak_tension_turn DOUBLE PRECISION NOT NULL,
    average_min_hand_size DOUBLE PRECISION NOT NULL,
    low_hand_pressure_rate DOUBLE PRECISION NOT NULL,
    average_power_retention_rate DOUBLE PRECISION NOT NULL,
    average_power_destroyed_rate DOUBLE PRECISION NOT NULL,
    average_final_alive_power DOUBLE PRECISION NOT NULL,
    average_information_events_per_game DOUBLE PRECISION NOT NULL,
    information_volatility_score DOUBLE PRECISION NOT NULL,
    average_turn_swing_score DOUBLE PRECISION NOT NULL,
    max_turn_swing_score DOUBLE PRECISION NOT NULL,
    high_swing_turn_rate DOUBLE PRECISION NOT NULL,
    average_threat_saturation DOUBLE PRECISION NOT NULL,
    max_threat_saturation DOUBLE PRECISION NOT NULL,
    turns_with_multiple_threatened_players DOUBLE PRECISION NOT NULL,
    pressure_distribution_entropy DOUBLE PRECISION NOT NULL,
    target_concentration DOUBLE PRECISION NOT NULL,
    most_pressured_player_share DOUBLE PRECISION NOT NULL,
    pressure_fairness_score DOUBLE PRECISION NOT NULL,
    average_lead_changes DOUBLE PRECISION NOT NULL,
    leader_retention_rate DOUBLE PRECISION NOT NULL,
    average_power_gap DOUBLE PRECISION NOT NULL,
    comeback_against_power_leader_rate DOUBLE PRECISION NOT NULL,
    momentum_stability_score DOUBLE PRECISION NOT NULL,
    average_decision_options_per_turn DOUBLE PRECISION NOT NULL,
    average_action_branching_factor DOUBLE PRECISION NOT NULL,
    average_reaction_opportunity_rate DOUBLE PRECISION NOT NULL,
    decision_density_score DOUBLE PRECISION NOT NULL,
    memorable_event_count INTEGER NOT NULL,
    memorable_events_per_game DOUBLE PRECISION NOT NULL,
    memorability_score DOUBLE PRECISION NOT NULL
);

CREATE TABLE IF NOT EXISTS card_gravity_metric_summaries (
    run_id UUID NOT NULL REFERENCES simulation_runs(id),
    card_version_code TEXT NOT NULL,
    average_turns_held_before_play DOUBLE PRECISION NOT NULL,
    average_turns_held_before_discard DOUBLE PRECISION NOT NULL,
    kept_until_end_rate DOUBLE PRECISION NOT NULL,
    reaction_held_rate DOUBLE PRECISION NOT NULL,
    high_power_card_retention_rate DOUBLE PRECISION NOT NULL,
    times_card_in_hand_when_player_was_targeted INTEGER NOT NULL,
    card_gravity_score DOUBLE PRECISION NOT NULL,
    PRIMARY KEY (run_id, card_version_code)
);

CREATE TABLE IF NOT EXISTS turn_curve_metric_summaries (
    run_id UUID NOT NULL REFERENCES simulation_runs(id),
    turn_number INTEGER NOT NULL,
    average_hand_size DOUBLE PRECISION NOT NULL,
    min_hand_size DOUBLE PRECISION NOT NULL,
    max_hand_size DOUBLE PRECISION NOT NULL,
    hand_size_stddev DOUBLE PRECISION NOT NULL,
    players_at_1_card DOUBLE PRECISION NOT NULL,
    threat_saturation DOUBLE PRECISION NOT NULL,
    PRIMARY KEY (run_id, turn_number)
);
