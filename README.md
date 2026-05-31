# Power Bulletin Service

`pb-service` is the Tanuki/Spring Boot backend for canonical Power Bulletin data.
It manages cards, card versions, print sets, ordered print powers, effect metadata, decks,
deck versions, and deck entries.

The Python simulator still owns gameplay execution. This service only stores and exports
canonical data that the simulator and CMS can consume.

The service uses Tanuki Core `0.0.4-SNAPSHOT` for CRUD controllers, response
wrappers, pagination metadata, validation errors, and JWT principal wiring.

## Relationship To Other Projects

- `pb-simulator`: runs simulations and executes Python effect classes from `EffectDefinition.pythonClass`.
- `pb-cms`: should manage content through this service instead of keeping separate card/deck shapes.
- `pb-service`: owns the canonical CRUD API and database structure for content records.

## Main Tables

- `card_identities`
- `card_versions`
- `card_print_sets`
- `card_print_set_powers`
- `effect_definitions`
- `deck_identities`
- `deck_versions`
- `deck_entries`

`CardCopy` remains runtime-only. It is generated from:

```text
CardCopy -> CardPrintSet -> CardVersion -> CardIdentity
```

## Local Run

```bash
./gradlew bootRun
```

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

## Build And Test

```bash
./gradlew test
./gradlew build
```

## PostgreSQL

Use these environment variables for PostgreSQL:

```bash
DATABASE_URL=jdbc:postgresql://localhost:5432/power_bulletin
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=
DATABASE_DRIVER=org.postgresql.Driver
HIBERNATE_DIALECT=org.hibernate.dialect.PostgreSQLDialect
JPA_DDL_AUTO=validate
FLYWAY_ENABLED=true
```

The migration keeps legacy `card_print_sets.powers` as a compatibility column and copies it into
`card_print_set_powers` when present. New Java code treats `card_print_set_powers` as canonical.

## Useful Endpoints

- `GET /cards`
- `GET /cards/code/{code}`
- `GET /card-versions/by-card/{cardIdentityId}`
- `GET /card-print-sets/by-version/{cardVersionId}`
- `GET /effects/code/{code}`
- `GET /decks/code/{code}`
- `GET /deck-versions/code/{code}`
- `GET /deck-entries/by-version/{deckVersionId}`
- `GET /exports/cards/tree`
- `GET /exports/deck-versions/{deckVersionId}`
- `GET /exports/deck-versions/code/{code}`
- `GET /exports/base-game`
- `GET /cms/simulation-runs/{runId}` includes optional card interaction metric arrays when
  the simulator has written them: `cardPairMetrics`, `cardSequenceMetrics`,
  `cardCounterMetrics`, `reactionInteractionMetrics`, `onDiscardChainMetrics`,
  `powerPressureInteractionMetrics`, and `cardInteractionSummaryMetrics`.
- `GET /cms/simulation-run-groups`
- `GET /cms/simulation-run-groups/{groupId}`
- `GET /cms/simulation-run-groups/{groupId}/summary`
- `GET /cms/simulation-run-groups/{groupId}/subruns`

Grouped runs are parent runs containing standard 2P, 3P, and 4P subruns. They are designed to make
cross-player-count comparison easier while keeping individual subrun result endpoints unchanged.

## Seed Data

The service seeds the known base and Head Office records by default. Disable it with:

```bash
SEED_POWER_BULLETIN_ENABLED=false
```

## User-Service Permissions

This service owns its user-service permission seed script:

```bash
./scripts/seed-user-service-permissions.sh
```

The script prompts for the user-service database connection, then idempotently
seeds the `POWER_BULLETIN` microservice, CRUD feature codes used by this
service's Tanuki controllers, standard CRUD subfeatures, and `SUPER_ADMIN` ACLs
when that role exists.
