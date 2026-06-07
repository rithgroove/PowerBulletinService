ALTER TABLE simulation_run_groups
    ADD COLUMN IF NOT EXISTS deck_identity_id UUID;

UPDATE simulation_run_groups srg
SET deck_identity_id = resolved.deck_identity_id
FROM (
    SELECT
        srg_inner.id AS run_group_id,
        MIN(dv.deck_identity_id::text)::uuid AS deck_identity_id
    FROM simulation_run_groups srg_inner
    JOIN simulation_runs sr ON sr.run_group_id = srg_inner.id
    JOIN deck_versions dv ON dv.id = sr.deck_version_id
    WHERE srg_inner.deck_identity_id IS NULL
    GROUP BY srg_inner.id
) resolved
WHERE srg.id = resolved.run_group_id
  AND srg.deck_identity_id IS NULL;

UPDATE simulation_run_groups srg
SET deck_identity_id = di.id
FROM deck_identities di
WHERE srg.deck_identity_id IS NULL
  AND di.deleted_at IS NULL
  AND di.code = srg.deck_code;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'simulation_run_groups_deck_identity_id_fkey'
    ) THEN
        ALTER TABLE simulation_run_groups
            ADD CONSTRAINT simulation_run_groups_deck_identity_id_fkey
            FOREIGN KEY (deck_identity_id) REFERENCES deck_identities(id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_simulation_run_groups_deck_identity_id
    ON simulation_run_groups(deck_identity_id);
