# Project Guidelines

## Project Scope

- This repository builds the Java 17 `util` library, a general-purpose utility library with a deliberately limited dependency set.
- Keep shared utility code here. Put features that require larger or optional frameworks and integrations in SJavaDepsClass instead of expanding this library's dependencies for a specialized use case.
- Treat public classes and methods as a reusable library API. Avoid unnecessary breaking changes and document public API additions with Javadoc.

## Development

- Keep Java sources under `src/main/java` and tests under `src/test/java`.
- Use the existing Maven configuration and Java 17 source and target level.
- Run `mvn test` from the repository root after code changes.
- Do not edit generated output under `target/`.
