CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS effect_definitions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code TEXT NOT NULL UNIQUE,
    python_class TEXT NOT NULL,
    name TEXT NOT NULL,
    description TEXT NOT NULL,
    status TEXT,
    created_at TIMESTAMP,
    created_by_id UUID,
    created_by_name TEXT,
    updated_at TIMESTAMP,
    updated_by_id UUID,
    updated_by_name TEXT,
    deleted_at TIMESTAMP,
    deleted_by_id UUID,
    deleted_by_name TEXT
);

CREATE TABLE IF NOT EXISTS card_identities (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    character_name TEXT NOT NULL,
    faction TEXT NOT NULL,
    notes TEXT,
    status TEXT,
    created_at TIMESTAMP,
    created_by_id UUID,
    created_by_name TEXT,
    updated_at TIMESTAMP,
    updated_by_id UUID,
    updated_by_name TEXT,
    deleted_at TIMESTAMP,
    deleted_by_id UUID,
    deleted_by_name TEXT
);

CREATE TABLE IF NOT EXISTS card_versions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code TEXT NOT NULL UNIQUE,
    card_identity_id UUID NOT NULL REFERENCES card_identities(id),
    effect_definition_id UUID NOT NULL REFERENCES effect_definitions(id),
    version_name TEXT NOT NULL,
    card_type TEXT NOT NULL,
    effect_text TEXT NOT NULL,
    status TEXT,
    created_at TIMESTAMP,
    created_by_id UUID,
    created_by_name TEXT,
    updated_at TIMESTAMP,
    updated_by_id UUID,
    updated_by_name TEXT,
    deleted_at TIMESTAMP,
    deleted_by_id UUID,
    deleted_by_name TEXT
);

CREATE TABLE IF NOT EXISTS card_print_sets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code TEXT NOT NULL UNIQUE,
    card_version_id UUID NOT NULL REFERENCES card_versions(id),
    powers INTEGER[],
    status TEXT,
    created_at TIMESTAMP,
    created_by_id UUID,
    created_by_name TEXT,
    updated_at TIMESTAMP,
    updated_by_id UUID,
    updated_by_name TEXT,
    deleted_at TIMESTAMP,
    deleted_by_id UUID,
    deleted_by_name TEXT
);

CREATE TABLE IF NOT EXISTS card_print_set_powers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    card_print_set_id UUID NOT NULL REFERENCES card_print_sets(id) ON DELETE CASCADE,
    power_order INTEGER NOT NULL,
    power INTEGER NOT NULL,
    CONSTRAINT card_print_set_powers_print_set_order_key UNIQUE (card_print_set_id, power_order)
);

INSERT INTO card_print_set_powers (id, card_print_set_id, power_order, power)
SELECT gen_random_uuid(), cps.id, p.ordinality::INTEGER, p.power
FROM card_print_sets cps
CROSS JOIN LATERAL unnest(cps.powers) WITH ORDINALITY AS p(power, ordinality)
WHERE cps.powers IS NOT NULL
AND NOT EXISTS (
    SELECT 1
    FROM card_print_set_powers existing
    WHERE existing.card_print_set_id = cps.id
);

CREATE TABLE IF NOT EXISTS deck_identities (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    notes TEXT,
    status TEXT,
    created_at TIMESTAMP,
    created_by_id UUID,
    created_by_name TEXT,
    updated_at TIMESTAMP,
    updated_by_id UUID,
    updated_by_name TEXT,
    deleted_at TIMESTAMP,
    deleted_by_id UUID,
    deleted_by_name TEXT
);

CREATE TABLE IF NOT EXISTS deck_versions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code TEXT NOT NULL UNIQUE,
    deck_identity_id UUID NOT NULL REFERENCES deck_identities(id),
    version_name TEXT NOT NULL,
    notes TEXT,
    status TEXT,
    created_at TIMESTAMP,
    created_by_id UUID,
    created_by_name TEXT,
    updated_at TIMESTAMP,
    updated_by_id UUID,
    updated_by_name TEXT,
    deleted_at TIMESTAMP,
    deleted_by_id UUID,
    deleted_by_name TEXT
);

CREATE TABLE IF NOT EXISTS deck_entries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    deck_version_id UUID NOT NULL REFERENCES deck_versions(id),
    card_print_set_id UUID NOT NULL REFERENCES card_print_sets(id),
    quantity INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP,
    created_by_id UUID,
    created_by_name TEXT,
    updated_at TIMESTAMP,
    updated_by_id UUID,
    updated_by_name TEXT,
    deleted_at TIMESTAMP,
    deleted_by_id UUID,
    deleted_by_name TEXT,
    CONSTRAINT deck_entries_version_print_set_key UNIQUE (deck_version_id, card_print_set_id)
);

CREATE INDEX IF NOT EXISTS idx_card_versions_card_identity_id ON card_versions(card_identity_id);
CREATE INDEX IF NOT EXISTS idx_card_versions_effect_definition_id ON card_versions(effect_definition_id);
CREATE INDEX IF NOT EXISTS idx_card_print_sets_card_version_id ON card_print_sets(card_version_id);
CREATE INDEX IF NOT EXISTS idx_card_print_set_powers_card_print_set_id ON card_print_set_powers(card_print_set_id);
CREATE INDEX IF NOT EXISTS idx_deck_versions_deck_identity_id ON deck_versions(deck_identity_id);
CREATE INDEX IF NOT EXISTS idx_deck_entries_deck_version_id ON deck_entries(deck_version_id);
CREATE INDEX IF NOT EXISTS idx_deck_entries_card_print_set_id ON deck_entries(card_print_set_id);
