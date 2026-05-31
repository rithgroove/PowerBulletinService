# AGENTS.md

## Purpose

This package contains Power Bulletin service-layer business rules, validation, CRUD orchestration, CMS queries, export shaping, and product behavior.

## How This Folder Fits the Workspace

Services are the main place for backend rules in `pb-service`. They may expose simulator output and canonical data, but they must not execute game rules owned by `pb-simulator`.

## Coding Conventions

- CRUD services should extend Tanuki base services and implement validation hooks.
- Keep validation in service methods using Tanuki `ValidationError` / `ValidationErrorException`.
- Use repositories for persistence only.
- Keep export DTO shaping explicit and stable.
- Allowlist sort keys for custom query services before building SQL.

## Rules for Future Agents

- Do not fake metrics, narratives, or comparison output.
- Do not add simulator runtime types as service-layer execution state.
- Keep `PowerBulletinCmsQueryService` focused on read models for admin/result screens.
- Keep `PowerBulletinExportService` focused on resolved export shapes for downstream consumers.

## Testing / Verification

Run `./gradlew test` after service changes. Add focused tests for validation, DTO mapping, export responses, custom sorting/filtering/search, and product/result behavior.

## Common Risks

- Accidentally moving gameplay rules from Python into Java.
- Returning response shapes that no longer match Tanuki FE list expectations.
- Validation behavior split between controller and service.
