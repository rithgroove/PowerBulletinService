ALTER TABLE simulation_run_groups
    ADD COLUMN IF NOT EXISTS deck_version_id UUID REFERENCES deck_versions(id),
    ADD COLUMN IF NOT EXISTS queue_status TEXT NOT NULL DEFAULT 'COMPLETED',
    ADD COLUMN IF NOT EXISTS queued_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS claimed_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS completed_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS failed_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS worker_id TEXT,
    ADD COLUMN IF NOT EXISTS failure_message TEXT;

UPDATE simulation_run_groups srg
SET deck_version_id = resolved.deck_version_id
FROM (
    SELECT DISTINCT ON (srg_inner.id)
        srg_inner.id AS run_group_id,
        sr.deck_version_id
    FROM simulation_run_groups srg_inner
    JOIN simulation_runs sr ON sr.run_group_id = srg_inner.id
    WHERE sr.deck_version_id IS NOT NULL
    ORDER BY srg_inner.id, sr.created_at ASC
) resolved
WHERE srg.id = resolved.run_group_id
  AND srg.deck_version_id IS NULL;

UPDATE simulation_run_groups
SET queue_status = 'COMPLETED',
    completed_at = COALESCE(completed_at, created_at)
WHERE queue_status IS NULL OR queue_status = '';

CREATE INDEX IF NOT EXISTS idx_simulation_run_groups_queue_status
    ON simulation_run_groups(queue_status, queued_at, created_at);

CREATE INDEX IF NOT EXISTS idx_simulation_run_groups_deck_version_id
    ON simulation_run_groups(deck_version_id);
