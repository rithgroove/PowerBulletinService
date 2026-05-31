# AGENTS.md

## Purpose

This package contains REST controllers for Power Bulletin CRUD resources, CMS read models, exports, products, and simulation result views.

## How This Folder Fits the Workspace

Controllers are HTTP boundaries only. Business rules belong in `service`, persistence in `repository`, and gameplay execution in `pb-simulator`.

## Coding Conventions

- Use constructor injection.
- Standard CRUD controllers should extend Tanuki `MasterController` or `BaseController`.
- Override `getService()` and `getFeatureCode()` for CRUD controllers.
- Return Tanuki response envelopes.
- Keep custom endpoints thin and delegate to services.

## Rules for Future Agents

- Do not add gameplay execution here.
- Do not bypass service validation or permission checks.
- Do not mix export behavior into CRUD controllers when `ExportController` is the clearer boundary.
- Do not put website-specific pagination logic in controllers.

## Testing / Verification

Run `./gradlew test` after controller changes. Add controller or service tests for route contract, permission-sensitive behavior, and error handling changes.

## Common Risks

- Route drift that breaks `npg-website` clients.
- Permission feature codes diverging from user-service seed scripts.
- Growing controllers into business-logic classes.
