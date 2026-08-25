# AGENTS.md

- This repo is a single-module Spring Boot app; there is no separate workspace or package manager setup. The app entrypoint is `src/main/java/com/wallet/account/AccountApplication.java`, with web/API code under `src/main/java/com/wallet/account/infrastructure/adapters/in/web`, domain logic under `src/main/java/com/wallet/account/domain`, and persistence under `src/main/java/com/wallet/account/infrastructure/adapters/out/db`.
- Use the Maven wrapper from the repo root: `./mvnw ...`. The build is defined in `pom.xml`, and the project explicitly targets Java 25 (`<java.version>25</java.version>`). The Dockerfile also builds/runs on Temurin 25.
- Standard verification command: `./mvnw test`. There is no separate lint/typecheck task configured in this repo; if you need a single test, use `./mvnw -Dtest=AccountApplicationTests test`.
- Local runtime is not self-contained: `src/main/resources/application.properties` points to PostgreSQL at `localhost:5432/accounts` with user `admin` and password `SamplePassword123`. Flyway is enabled; do not assume Hibernate creates tables.
- App port is `8082` (`server.port=8082`), and endpoints are mounted under `/api/accounts` via `AccountController`. Security config in `AppConfig.java` explicitly disables default auth and allows `/api/accounts/**`.
- The repo uses a hexagonal/domain-driven layout: `AccountService` orchestrates use cases, repository abstractions live in `domain/ports/*`, and the Postgres adapter implements persistence via `SpringDataPostgresAccountRepository`.
- Important gotcha: Lombok is used throughout (`@Builder`, `@Slf4j`, record-based domain model). If Maven reports `cannot find symbol: log` or `builder()`, check the Java/Lombok annotation-processing setup before changing business logic.
- No README is present at the root; trust the executable build config and Spring config files over prose when they differ.
