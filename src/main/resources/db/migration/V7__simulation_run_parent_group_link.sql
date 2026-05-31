ALTER TABLE simulation_runs
    ADD COLUMN IF NOT EXISTS run_group_id UUID REFERENCES simulation_run_groups(id);

UPDATE simulation_runs sr
SET run_group_id = srgm.run_group_id
FROM simulation_run_group_members srgm
WHERE srgm.run_id = sr.id
  AND sr.run_group_id IS NULL;

CREATE INDEX IF NOT EXISTS idx_simulation_runs_run_group_id
    ON simulation_runs(run_group_id);
