# AGENTS.md

## Purpose

`pb-service` is the Java Spring Boot / Tanuki backend for canonical Power Bulletin data, admin APIs, export APIs, product catalog records, and persisted simulation result views.

It is service-level application code, not reusable Tanuki framework code and not the gameplay engine.

## How This Folder Fits the Workspace

- `pb-simulator` owns gameplay execution, effects, bots, metric generation, and replay logs.
- `pb-service` owns the canonical API/schema boundary for Power Bulletin data consumed by the simulator, CMS tools, and `npg-website`.
- `npg-website` renders admin/result pages by calling this service.
- `pb-cms` is a useful reference for analysis workflows that may need explicit service endpoints here.
- Reusable backend framework behavior belongs in `tanuki-core`; reusable server-rendered list UI belongs in `tanuki-fe`.

## Important Files and Folders

- `src/main/java/com/nopunnygames/pbservice/controller`: REST controllers for CRUD, CMS queries, exports, products, and results.
- `src/main/java/com/nopunnygames/pbservice/service`: CRUD services, `PowerBulletinCmsQueryService`, `PowerBulletinExportService`, and product services.
- `src/main/java/com/nopunnygames/pbservice/repository`: Spring Data repositories only.
- `src/main/java/com/nopunnygames/pbservice/entity`: JPA entities and DTO conversion.
- `src/main/java/com/nopunnygames/pbservice/dto`: explicit API DTOs for cards, decks, effects, products, exports, and resolved deck entries.
- `src/main/java/com/nopunnygames/pbservice/config/PowerBulletinSeeder.java`: seed data for known Power Bulletin records.
- `src/main/resources/db/migration`: Flyway migrations.
- `scripts/seed-user-service-permissions.sh`: user-service permission seeding for this service.
- `src/test/java/com/nopunnygames/pbservice`: Spring/JUnit tests.

## Development Commands

From Gradle config and README:

```bash
./gradlew test
./gradlew javadoc
./gradlew build
./gradlew bootRun
```

Typical PostgreSQL local run:

```bash
SERVER_PORT=8083 DATABASE_URL=jdbc:postgresql://localhost:5432/power_bulletin DATABASE_USERNAME=postgres DATABASE_PASSWORD=supersecret DATABASE_DRIVER=org.postgresql.Driver HIBERNATE_DIALECT=org.hibernate.dialect.PostgreSQLDialect JPA_DDL_AUTO=validate FLYWAY_ENABLED=true ./gradlew bootRun
```

`bootRun` reads `.env` when present.

## Coding Conventions

Use the Tanuki flow:

```text
DTO -> Controller -> Service -> Repository -> Entity -> DTO
```

Observed conventions:

- CRUD controllers extend Tanuki `MasterController` or `BaseController` and override `getService()` and `getFeatureCode()`.
- Services extend Tanuki `MasterService`/`BaseService` and own validation/business rules.
- Repositories extend Tanuki repository types and stay persistence-only.
- Entities own JPA mapping plus deliberate `toDto()` / `toCompleteDto()` conversion.
- API responses use `ApiResponse`, `ApiErrorResponse`, `PagedResponse`, and `PageMeta`.
- Validation uses `ValidationError` and `ValidationErrorException`.
- `CardPrintSetPower` is the canonical ordered print-power model; legacy `card_print_sets.powers` is compatibility data.
- CMS query endpoints may use explicit read models/JDBC, but must keep Tanuki-compatible response shapes.

Current migrations include:

- `V1__canonical_power_bulletin_schema.sql`
- `V2__simulation_metric_tables.sql`
- `V3__product_catalog_tables.sql`

## Rules for Future Agents

- Do not implement simulator execution, bot decisions, or effect behavior here.
- Do not fake simulation metrics or narrative output; expose persisted simulator data or explicit analysis services.
- Keep controllers thin and delegate validation/workflows to services.
- Keep `CmsQueryController` focused on read models for admin/result screens.
- Keep `ExportController` focused on resolved simulator/export shapes.
- Keep CRUD feature codes and `scripts/seed-user-service-permissions.sh` aligned.
- Preserve Tanuki response wrappers and route contracts unless intentionally changing an API.
- Use allowlisted sort fields before building SQL for custom list endpoints.
- Add new schema changes as append-only Flyway migrations unless explicitly instructed otherwise.

Feature codes currently documented for permission seeding include:

```text
CARD_IDENTITY
CARD_VERSION
CARD_PRINT_SET
EFFECT_DEFINITION
DECK_IDENTITY
DECK_VERSION
DECK_ENTRY
PB_PRODUCT
PB_RECORDS
```

Use Tanuki action suffixes such as `INDEX`, `READ`, `CREATE`, `UPDATE`, `DELETE`, `ACTIVATE`, and `DEACTIVATE`.

## Testing / Verification

Run after service changes:

```bash
./gradlew test
```

Run `./gradlew build` for larger changes. Add focused tests when changing service validation, entity/DTO mapping, CMS query sort/filter/search behavior, exports, Flyway schema behavior, permission-sensitive CRUD behavior, product APIs, or simulation result query behavior.

## Common Risks

- Moving game rules out of `pb-simulator`.
- Letting Java schema assumptions drift away from simulator writers/readers.
- Returning website-specific list shapes instead of Tanuki-compatible pages.
- Forgetting to update permission seed scripts when adding resources.
- Creating recursive DTO conversion or accidental over-fetching through entity relationships.
- Using raw SQL sort/filter fields without allowlisting.
