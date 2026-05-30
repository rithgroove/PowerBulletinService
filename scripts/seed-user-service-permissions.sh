#!/usr/bin/env bash
set -euo pipefail

WORKSPACE_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
USER_SERVICE_ENV="$WORKSPACE_ROOT/user-service/.env"
if [[ -f "$USER_SERVICE_ENV" ]]; then
  set -a
  # shellcheck disable=SC1090
  source "$USER_SERVICE_ENV"
  set +a
fi

prompt() {
  local name="$1"
  local default="$2"
  local value
  read -r -p "$name [$default]: " value
  printf '%s' "${value:-$default}"
}

DB_HOST="$(prompt "User-service DB host" "${PB_DB_HOST:-localhost}")"
DB_PORT="$(prompt "User-service DB port" "${PB_DB_PORT:-5432}")"
DB_NAME="$(prompt "User-service DB name" "${PB_DB_NAME:-user_service_db}")"
DB_USER="$(prompt "User-service DB user" "${PB_DB_USER:-${DATABASE_USERNAME:-postgres}}")"
DB_PASSWORD="${PB_DB_PASSWORD:-${DATABASE_PASSWORD:-}}"
if [[ -z "$DB_PASSWORD" ]]; then
  read -r -s -p "User-service DB password: " DB_PASSWORD
  printf '\n'
fi
SERVICE_URL="$(prompt "Power Bulletin service URL" "${POWER_BULLETIN_SERVICE_URL:-http://localhost:8083}")"

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
    WHERE (code IN ('POWER_BULLETIN', 'PB_SERVICE') OR name = 'Power Bulletin Service')
      AND deleted_at IS NULL
    ORDER BY CASE WHEN code = 'POWER_BULLETIN' THEN 0 ELSE 1 END
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
            code = 'POWER_BULLETIN',
            description = 'Power Bulletin canonical content and simulator result service',
            url = service_url,
            status = 'Active',
            updated_at = now()
        WHERE id = service_id;
    END IF;

    DELETE FROM roles_access_clearance_level acl
    USING subfeatures sf, features f
    WHERE acl.sub_features_id = sf.id
      AND sf.feature_id = f.id
      AND f.micro_service_id = service_id
      AND f.code = 'POWERBULLETIN';

    DELETE FROM subfeatures sf
    USING features f
    WHERE sf.feature_id = f.id
      AND f.micro_service_id = service_id
      AND f.code = 'POWERBULLETIN';

    DELETE FROM features
    WHERE micro_service_id = service_id
      AND code = 'POWERBULLETIN';

    FOR feature_row IN
        SELECT *
        FROM (VALUES
            ('CARD_IDENTITY', 'Card Identity', 'Power Bulletin card identity records', ARRAY['INDEX','READ','CREATE','UPDATE','DELETE','ACTIVATE','DEACTIVATE']),
            ('CARD_VERSION', 'Card Version', 'Power Bulletin card version records', ARRAY['INDEX','READ','CREATE','UPDATE','DELETE','ACTIVATE','DEACTIVATE']),
            ('CARD_PRINT_SET', 'Card Print Set', 'Power Bulletin card print set records', ARRAY['INDEX','READ','CREATE','UPDATE','DELETE','ACTIVATE','DEACTIVATE']),
            ('EFFECT_DEFINITION', 'Effect Definition', 'Power Bulletin effect definition records', ARRAY['INDEX','READ','CREATE','UPDATE','DELETE','ACTIVATE','DEACTIVATE']),
            ('DECK_IDENTITY', 'Deck Identity', 'Power Bulletin deck identity records', ARRAY['INDEX','READ','CREATE','UPDATE','DELETE','ACTIVATE','DEACTIVATE']),
            ('DECK_VERSION', 'Deck Version', 'Power Bulletin deck version records', ARRAY['INDEX','READ','CREATE','UPDATE','DELETE','ACTIVATE','DEACTIVATE']),
            ('DECK_ENTRY', 'Deck Entry', 'Power Bulletin deck entry records', ARRAY['INDEX','READ','CREATE','UPDATE','DELETE','ACTIVATE','DEACTIVATE']),
            ('PB_PRODUCT', 'Power Bulletin Product', 'Power Bulletin product records', ARRAY['INDEX','READ','CREATE','UPDATE','DELETE','ACTIVATE','DEACTIVATE']),
            ('PB_RECORDS', 'Power Bulletin Records', 'Power Bulletin simulator run and metric records', ARRAY['INDEX','READ'])
        ) AS seed(code, name, description, permissions)
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
            WHERE seed.code = ANY(feature_row.permissions)
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
