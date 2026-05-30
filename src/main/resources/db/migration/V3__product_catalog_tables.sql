CREATE TABLE IF NOT EXISTS products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    description TEXT,
    product_type TEXT NOT NULL,
    release_status TEXT NOT NULL,
    display_order INTEGER NOT NULL DEFAULT 0,
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

CREATE TABLE IF NOT EXISTS product_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL REFERENCES products(id),
    card_print_set_id UUID NOT NULL REFERENCES card_print_sets(id),
    quantity INTEGER NOT NULL DEFAULT 1,
    sort_order INTEGER NOT NULL DEFAULT 0,
    status TEXT,
    created_at TIMESTAMP,
    created_by_id UUID,
    created_by_name TEXT,
    updated_at TIMESTAMP,
    updated_by_id UUID,
    updated_by_name TEXT,
    deleted_at TIMESTAMP,
    deleted_by_id UUID,
    deleted_by_name TEXT,
    CONSTRAINT product_items_quantity_check CHECK (quantity > 0)
);

CREATE TABLE IF NOT EXISTS deck_version_products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    deck_version_id UUID NOT NULL REFERENCES deck_versions(id),
    product_id UUID NOT NULL REFERENCES products(id),
    quantity_multiplier INTEGER NOT NULL DEFAULT 1,
    applied_at TIMESTAMP NOT NULL,
    status TEXT,
    created_at TIMESTAMP,
    created_by_id UUID,
    created_by_name TEXT,
    updated_at TIMESTAMP,
    updated_by_id UUID,
    updated_by_name TEXT,
    deleted_at TIMESTAMP,
    deleted_by_id UUID,
    deleted_by_name TEXT,
    CONSTRAINT deck_version_products_quantity_multiplier_check CHECK (quantity_multiplier > 0)
);

CREATE INDEX IF NOT EXISTS idx_products_code ON products(code);
CREATE INDEX IF NOT EXISTS idx_products_product_type ON products(product_type);
CREATE INDEX IF NOT EXISTS idx_products_release_status ON products(release_status);
CREATE INDEX IF NOT EXISTS idx_product_items_product_id ON product_items(product_id);
CREATE INDEX IF NOT EXISTS idx_product_items_card_print_set_id ON product_items(card_print_set_id);
CREATE INDEX IF NOT EXISTS idx_deck_version_products_deck_version_id ON deck_version_products(deck_version_id);
CREATE INDEX IF NOT EXISTS idx_deck_version_products_product_id ON deck_version_products(product_id);

CREATE UNIQUE INDEX IF NOT EXISTS idx_product_items_active_product_print_set_key
ON product_items(product_id, card_print_set_id)
WHERE deleted_at IS NULL AND LOWER(status) = 'active';

CREATE UNIQUE INDEX IF NOT EXISTS idx_deck_version_products_active_deck_product_key
ON deck_version_products(deck_version_id, product_id)
WHERE deleted_at IS NULL AND LOWER(status) = 'active';
