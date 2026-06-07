ALTER TABLE simulation_run_groups
    ADD COLUMN IF NOT EXISTS approved_at TIMESTAMPTZ;

CREATE INDEX IF NOT EXISTS idx_simulation_run_groups_approved_at
    ON simulation_run_groups(approved_at);
