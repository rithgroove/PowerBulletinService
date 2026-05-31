ALTER TABLE reaction_interaction_metrics
    ADD COLUMN IF NOT EXISTS targeted_reaction_count INTEGER,
    ADD COLUMN IF NOT EXISTS reacting_player_was_target_count INTEGER;

UPDATE reaction_interaction_metrics
SET targeted_reaction_count = CASE WHEN targeted_reaction THEN COALESCE(reaction_count, 0) ELSE 0 END
WHERE targeted_reaction_count IS NULL;

UPDATE reaction_interaction_metrics
SET reacting_player_was_target_count = CASE WHEN reacting_player_was_target THEN COALESCE(reaction_count, 0) ELSE 0 END
WHERE reacting_player_was_target_count IS NULL;

ALTER TABLE power_pressure_interaction_metrics
    ADD COLUMN IF NOT EXISTS removal_count INTEGER;

UPDATE power_pressure_interaction_metrics
SET removal_count = COALESCE(removed_count, 0)
WHERE removal_count IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS ux_card_pair_metrics_run_cards
    ON card_pair_metrics(run_id, card_a_code, card_b_code);

CREATE UNIQUE INDEX IF NOT EXISTS ux_card_sequence_metrics_run_cards_type
    ON card_sequence_metrics(run_id, source_card_code, followup_card_code, sequence_type);

CREATE UNIQUE INDEX IF NOT EXISTS ux_card_counter_metrics_run_cards_type
    ON card_counter_metrics(run_id, counter_card_code, countered_card_code, counter_type);

CREATE UNIQUE INDEX IF NOT EXISTS ux_reaction_interaction_metrics_run_cards
    ON reaction_interaction_metrics(run_id, reaction_card_code, pending_effect_card_code);

CREATE UNIQUE INDEX IF NOT EXISTS ux_on_discard_chain_metrics_run_cards
    ON on_discard_chain_metrics(run_id, discard_source_card_code, discarded_card_code, triggered_card_code);

CREATE UNIQUE INDEX IF NOT EXISTS ux_power_pressure_interaction_metrics_run_cards_power
    ON power_pressure_interaction_metrics(
        run_id,
        source_card_code,
        removed_card_code,
        removed_card_power,
        removed_card_faction,
        removed_card_type,
        was_high_power_card
    );

CREATE UNIQUE INDEX IF NOT EXISTS ux_card_interaction_summary_metrics_run_card
    ON card_interaction_summary_metrics(run_id, card_code);
