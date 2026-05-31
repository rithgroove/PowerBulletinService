# AGENTS.md

## Purpose

This folder contains Flyway migrations for the canonical Power Bulletin service schema.

## How This Folder Fits the Workspace

The schema supports Java service APIs and must stay compatible with simulator data producers/readers where the projects share tables or concepts.

## Important Files and Folders

- `V1__canonical_power_bulletin_schema.sql`: cards, decks, effects, and canonical Power Bulletin structures.
- `V2__simulation_metric_tables.sql`: persisted simulation metric/result tables.
- `V3__product_catalog_tables.sql`: Power Bulletin product catalog tables.

## Coding Conventions

- Migrations are append-only by default.
- Use PostgreSQL-compatible SQL.
- Keep table/column names aligned with JPA entities.
- Preserve compatibility columns only when existing code/data still needs them.

## Rules for Future Agents

- Do not rewrite old migrations unless explicitly requested.
- Do not make destructive schema changes without a clear migration strategy.
- Check `pb-simulator` database code before changing shared result/card/deck assumptions.
- Update entities, DTOs, services, seeders, and docs with schema changes.

## Testing / Verification

Run `./gradlew test` for context/schema-sensitive tests. Run against a safe local PostgreSQL database for migration validation when changing SQL.

## Common Risks

- Java entities and SQL drifting apart.
- H2 passing while PostgreSQL behavior fails.
- Breaking simulator or CMS readers through renamed columns.
