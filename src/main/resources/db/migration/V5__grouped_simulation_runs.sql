CREATE TABLE IF NOT EXISTS simulation_run_groups (
    id UUID PRIMARY KEY,
    run_group_code TEXT NOT NULL UNIQUE,
    run_group_name TEXT NOT NULL,
    deck_code TEXT NOT NULL,
    deck_name TEXT,
    version_name TEXT,
    rng_seed BIGINT,
    requested_iterations_per_player_count INTEGER NOT NULL,
    player_counts JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    notes TEXT
);

CREATE TABLE IF NOT EXISTS simulation_run_group_members (
    id UUID PRIMARY KEY,
    run_group_id UUID NOT NULL REFERENCES simulation_run_groups(id),
    run_id UUID NOT NULL REFERENCES simulation_runs(id),
    run_code TEXT NOT NULL,
    player_count INTEGER NOT NULL,
    total_games INTEGER NOT NULL,
    rng_seed BIGINT,
    status TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT simulation_run_group_members_group_run_key UNIQUE (run_group_id, run_id)
);

CREATE TABLE IF NOT EXISTS simulation_run_group_summaries (
    id UUID PRIMARY KEY,
    run_group_id UUID NOT NULL UNIQUE REFERENCES simulation_run_groups(id),
    summary_json JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_simulation_run_group_members_group_id
    ON simulation_run_group_members(run_group_id);

CREATE INDEX IF NOT EXISTS idx_simulation_run_group_members_run_id
    ON simulation_run_group_members(run_id);
