ALTER TABLE deck_identities
    ADD COLUMN IF NOT EXISTS display_order INTEGER NOT NULL DEFAULT 0;

WITH ordered_decks AS (
    SELECT
        id,
        ROW_NUMBER() OVER (ORDER BY LOWER(name) ASC, LOWER(code) ASC) * 10 AS new_display_order
    FROM deck_identities
    WHERE deleted_at IS NULL
      AND display_order = 0
)
UPDATE deck_identities deck
SET display_order = ordered_decks.new_display_order
FROM ordered_decks
WHERE deck.id = ordered_decks.id;
