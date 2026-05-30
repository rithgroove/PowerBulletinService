#!/usr/bin/env bash
set -euo pipefail

prompt() {
  local name="$1"
  local default="$2"
  local value
  read -r -p "$name [$default]: " value
  printf '%s' "${value:-$default}"
}

DB_HOST="$(prompt "User-service DB host" "localhost")"
DB_PORT="$(prompt "User-service DB port" "5432")"
DB_NAME="$(prompt "User-service DB name" "user_service_db")"
DB_USER="$(prompt "User-service DB user" "postgres")"
read -r -s -p "User-service DB password: " DB_PASSWORD
printf '\n'
SERVICE_URL="$(prompt "Power Bulletin service URL" "http://localhost:8083")"

export PGPASSWORD="$DB_PASSWORD"
psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -v ON_ERROR_STOP=1 -v service_url="$SERVICE_URL" <<'SQL'
CREATE TEMP TABLE tanuki_seed_config (service_url text);
INSERT INTO tanuki_seed_config VALUES (:'service_url');

DO $$
DECLARE
    service_id uuid;
    admin_role_id uuid;
    current_feature_id uuid;
    subfeature_id uuid;
    service_url text;
    feature_row record;
    permission_row record;
BEGIN
    SELECT tanuki_seed_config.service_url INTO service_url FROM tanuki_seed_config LIMIT 1;

    SELECT id INTO admin_role_id
    FROM roles
    WHERE code = 'SUPER_ADMIN'
      AND deleted_at IS NULL
    LIMIT 1;

    SELECT id INTO service_id
    FROM micro_service
    WHERE code = 'POWER_BULLETIN'
      AND deleted_at IS NULL
    LIMIT 1;

    IF service_id IS NULL THEN
        service_id := uuid_in(md5(random()::text || clock_timestamp()::text)::cstring);
        INSERT INTO micro_service (id, name, code, description, url, status, created_at, updated_at)
        VALUES (
            service_id,
            'Power Bulletin Service',
            'POWER_BULLETIN',
            'Power Bulletin canonical content and simulator result service',
            service_url,
            'Active',
            now(),
            now()
        );
    ELSE
        UPDATE micro_service
        SET name = 'Power Bulletin Service',
            description = 'Power Bulletin canonical content and simulator result service',
            url = service_url,
            status = 'Active',
            updated_at = now()
        WHERE id = service_id;
    END IF;

    FOR feature_row IN
        SELECT *
        FROM (VALUES
            ('CARD_IDENTITY', 'Card Identity', 'Power Bulletin card identity records'),
            ('CARD_VERSION', 'Card Version', 'Power Bulletin card version records'),
            ('CARD_PRINT_SET', 'Card Print Set', 'Power Bulletin card print set records'),
            ('EFFECT_DEFINITION', 'Effect Definition', 'Power Bulletin effect definition records'),
            ('DECK_IDENTITY', 'Deck Identity', 'Power Bulletin deck identity records'),
            ('DECK_VERSION', 'Deck Version', 'Power Bulletin deck version records'),
            ('DECK_ENTRY', 'Deck Entry', 'Power Bulletin deck entry records')
        ) AS seed(code, name, description)
    LOOP
        SELECT id INTO current_feature_id
        FROM features
        WHERE code = feature_row.code
          AND deleted_at IS NULL
        LIMIT 1;

        IF current_feature_id IS NULL THEN
            current_feature_id := uuid_in(md5(random()::text || clock_timestamp()::text)::cstring);
            INSERT INTO features (id, micro_service_id, name, code, description, status, created_at, updated_at)
            VALUES (current_feature_id, service_id, feature_row.name, feature_row.code, feature_row.description, 'Active', now(), now());
        ELSE
            UPDATE features
            SET micro_service_id = service_id,
                name = feature_row.name,
                description = feature_row.description,
                status = 'Active',
                updated_at = now()
            WHERE id = current_feature_id;
        END IF;

        FOR permission_row IN
            SELECT *
            FROM (VALUES
                ('INDEX', 'List', 'List ' || lower(feature_row.name) || ' records', false),
                ('READ', 'Read', 'Read one ' || lower(feature_row.name) || ' record', false),
                ('CREATE', 'Create', 'Create ' || lower(feature_row.name) || ' records', false),
                ('UPDATE', 'Update', 'Update ' || lower(feature_row.name) || ' records', false),
                ('DELETE', 'Delete', 'Delete ' || lower(feature_row.name) || ' records', false),
                ('ACTIVATE', 'Activate', 'Activate ' || lower(feature_row.name) || ' records', false),
                ('DEACTIVATE', 'Deactivate', 'Deactivate ' || lower(feature_row.name) || ' records', false)
            ) AS seed(code, name, description, public_access)
        LOOP
            SELECT id INTO subfeature_id
            FROM subfeatures
            WHERE feature_id = current_feature_id
              AND code = permission_row.code
              AND deleted_at IS NULL
            LIMIT 1;

            IF subfeature_id IS NULL THEN
                subfeature_id := uuid_in(md5(random()::text || clock_timestamp()::text)::cstring);
                INSERT INTO subfeatures (id, feature_id, code, name, description, public_access, status, created_at, updated_at)
                VALUES (subfeature_id, current_feature_id, permission_row.code, permission_row.name, permission_row.description, permission_row.public_access, 'Active', now(), now());
            ELSE
                UPDATE subfeatures
                SET name = permission_row.name,
                    description = permission_row.description,
                    public_access = permission_row.public_access,
                    status = 'Active',
                    updated_at = now()
                WHERE id = subfeature_id;
            END IF;

            IF admin_role_id IS NOT NULL AND NOT EXISTS (
                SELECT 1
                FROM roles_access_clearance_level
                WHERE role_id = admin_role_id
                  AND sub_features_id = subfeature_id
                  AND deleted_at IS NULL
            ) THEN
                INSERT INTO roles_access_clearance_level (id, role_id, sub_features_id, status, created_at, updated_at)
                VALUES (uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), admin_role_id, subfeature_id, 'Active', now(), now());
            END IF;
        END LOOP;
    END LOOP;
END $$;
SQL

printf 'Power Bulletin permissions seeded into user-service database.\n'
