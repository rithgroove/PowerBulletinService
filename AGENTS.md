# pb-service Agent Guide

## Repository Purpose

`pb-service` is the Java Spring Boot backend for canonical Power Bulletin data, CMS/admin screens, simulator export data, and simulator result viewing.

This repository contains service-level application code. It is not reusable Tanuki framework code. Keep framework behavior in `tanuki-core`, keep server-rendered UI helpers in `tanuki-fe`, and keep gameplay execution in `pb-simulator`.

Current responsibilities:

- Manage card identities, card versions, card print sets, ordered print powers, and effect definitions.
- Manage deck identities, deck versions, and deck entries.
- Expose Tanuki CRUD APIs for admin/CMS tools.
- Expose explicit CMS query endpoints for Tanuki FE-compatible list screens.
- Expose export endpoints consumed by simulator or website workflows.
- Store simulation metric tables through Flyway migrations.
- Seed canonical starter Power Bulletin data.
- Own the user-service permission seed script for this service.

## Tanuki Alignment

Use the Tanuki layer flow:

`DTO -> Controller -> Service -> Repository -> Entity -> DTO`

Use the actual Tanuki primitives already present:

- `MasterController` for status-aware CRUD controllers.
- `BaseController` for non-master CRUD controllers such as `DeckEntryController`.
- `MasterService` and `BaseService` for reusable CRUD behavior.
- `MasterRepository` and `BaseRepository` for persistence.
- `MasterEntity` and `BaseEntity` for entity base behavior.
- `ApiResponse`, `ApiErrorResponse`, `PagedResponse`, and `PageMeta` for response envelopes.
- `ValidationError` and `ValidationErrorException` for service validation.
- Tanuki security auto-configuration for JWT principal wiring.

Do not add controller-level public permission bypasses for CRUD endpoints. Public, export, simulator, or CMS-specific reads should be explicit endpoints with explicit service/query behavior.

## Package Roles

- `config`: OpenAPI config and `PowerBulletinSeeder`.
- `controller`: REST controllers. Keep CRUD, CMS query, and export endpoints clearly separated.
- `dto`: API DTOs for cards, decks, effects, exports, and resolved deck entries.
- `entity`: JPA entities and entity-to-DTO mapping logic.
- `enums`: Power Bulletin enum values such as `CardType` and `Faction`.
- `repository`: Spring Data repositories only.
- `service`: CRUD services, explicit CMS query service, and export service.
- `src/main/resources/db/migration`: Flyway schema migrations.
- `scripts`: operational scripts owned by this service.

## Controller Rules

CRUD controllers must extend Tanuki controllers directly:

- `CardIdentityController`, `CardVersionController`, `CardPrintSetController`, `EffectDefinitionController`, `DeckIdentityController`, and `DeckVersionController` extend `MasterController`.
- `DeckEntryController` extends `BaseController`.

Each CRUD controller must:

- Use constructor injection.
- Override `getService()`.
- Override `getFeatureCode()`.
- Keep custom endpoints thin and delegate to services or repositories only for simple lookup endpoints already established in this project.
- Preserve existing route names unless intentionally changing API contracts.

`CmsQueryController` is for explicit CMS read models. It should return Tanuki-compatible response envelopes and keep pagination, sorting, filtering, and search behavior predictable.

`ExportController` is for resolved simulator/export data. Do not mix export behavior into CRUD controllers.

## Permission Rules

Keep controller feature codes and user-service seed data aligned:

- `CARD_IDENTITY`
- `CARD_VERSION`
- `CARD_PRINT_SET`
- `EFFECT_DEFINITION`
- `DECK_IDENTITY`
- `DECK_VERSION`
- `DECK_ENTRY`

Use Tanuki action suffixes:

- `INDEX`
- `READ`
- `CREATE`
- `UPDATE`
- `DELETE`
- `ACTIVATE`
- `DEACTIVATE`

The service-owned permission script is:

```bash
./scripts/seed-user-service-permissions.sh
```

That script should seed the `POWER_BULLETIN` microservice, all feature codes above, standard CRUD subfeatures, and `SUPER_ADMIN` ACLs when that role exists. Update it whenever a new CRUD resource or feature code is added.

## API And List Contracts

Tanuki CRUD list endpoints should keep the standard shape:

```text
ApiResponse<PagedResponse<T>>
PagedResponse.items
PagedResponse.meta
```

Use standard Tanuki query parameters where possible:

```text
page
limit
search
sort_by
sort_direction
resource-specific filters
```

CMS query endpoints may use explicit JDBC read models, but they must still:

- Return `ApiResponse<PagedResponse<T>>` for paged lists.
- Use `sort_by` and `sort_direction`.
- Allowlist sort keys before using them in SQL.
- Keep filters explicit and domain-neutral enough for shared Tanuki FE list controls.
- Avoid website-specific pagination or filter logic.

## Entity And DTO Rules

Entities own JPA mapping and DTO conversion. Keep nested conversion deliberate:

- Use shallow `toDto()` for list-friendly responses.
- Use `toCompleteDto()` only where detail/export flows need relationship data.
- Avoid recursive relationship conversion and accidental over-fetching.
- Keep simulator runtime types out of JPA entities unless they are persisted records.

`CardPrintSetPower` is part of the canonical ordered print-power model. Do not treat legacy print-set power storage as the Java code path when the normalized table is available.

## Service Rules

Services own business rules and validation.

- CRUD services should use their Tanuki base service and implement required hooks.
- Put create/update validation in service validation methods.
- Use repositories for persistence only.
- Keep `PowerBulletinCmsQueryService` focused on explicit read queries for CMS screens.
- Keep `PowerBulletinExportService` focused on resolved export shapes for simulator/website consumers.
- Do not add gameplay simulation execution here.

## Security Rules

This service relies on Tanuki security auto-configuration. JWT claims should remain compatible with `user-service`:

- `user_id`
- `user_name`
- `acls`

Do not add a separate user database. Do not log secrets, bearer tokens, or credentials. Admin CRUD permission checks should be handled through Tanuki controller permission checks and seeded user-service ACLs.

## Database Rules

Flyway migrations live in `src/main/resources/db/migration`.

Current migrations include:

- `V1__canonical_power_bulletin_schema.sql`
- `V2__simulation_metric_tables.sql`

Rules:

- Keep migrations append-only unless explicitly instructed otherwise.
- Keep entity mappings aligned with schema changes.
- Use PostgreSQL-compatible schema design.
- H2 may be used for local tests, but production assumptions should be PostgreSQL-friendly.
- Avoid destructive schema changes without explicit instruction.

## Build And Run Commands

Supported commands:

```bash
./gradlew test
./gradlew javadoc
./gradlew build
./gradlew bootRun
```

`bootRun` reads `.env` when present.

Typical PostgreSQL run:

```bash
SERVER_PORT=8083 DATABASE_URL=jdbc:postgresql://localhost:5432/power_bulletin DATABASE_USERNAME=postgres DATABASE_PASSWORD=supersecret DATABASE_DRIVER=org.postgresql.Driver HIBERNATE_DIALECT=org.hibernate.dialect.PostgreSQLDialect JPA_DDL_AUTO=validate FLYWAY_ENABLED=true ./gradlew bootRun
```

## Testing Expectations

At minimum, keep the Spring context test passing.

Add focused tests when changing:

- CRUD service validation.
- Entity/DTO mapping.
- CMS query sorting/filtering/search behavior.
- Export response structure.
- Flyway schema behavior.
- Permission-sensitive CRUD behavior.
- Simulation result query behavior.

Run `./gradlew test` after changes. Run `./gradlew build` for broader verification before handing off significant changes.

## Change Discipline

- Keep changes small and explicit.
- Preserve API routes and response envelopes unless intentionally changing contracts.
- Update `README.md`, `AGENTS.md`, and seed scripts when resource names or feature codes change.
- Do not duplicate Tanuki CRUD behavior locally.
- Do not add dependencies casually.
- Do not move gameplay execution into this service.
- Do not bypass Tanuki permission checks for CRUD controllers.
- Do not add website-specific list behavior when Tanuki FE-compatible list contracts can serve the use case.
