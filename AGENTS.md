# AGENTS.md

- Single-module Spring Boot 4 app (Java 25). Entry point `src/main/java/com/wallet/account/AccountApplication.java`. Hexagonal layout: `domain` (model, ports), `application/services/AccountService` (use cases, `@Transactional`), `infrastructure` (web, db, messaging, security adapters). Domain and use-case ports must not import infrastructure classes.
- Build with the wrapper: `./mvnw test`. **Use JDK 25** (`JAVA_HOME=/usr/lib/jvm/java-25-openjdk`): Lombok crashes on newer JDKs (e.g. 27). Lombok runs through `annotationProcessorPaths` in `pom.xml`.
- `AccountApplicationTests` starts PostgreSQL and RabbitMQ with Testcontainers (skipped when Docker is unavailable): security end to end, concurrent withdrawals (guards the row lock), audit messages on the queue, DB constraints. Unit tests cover the domain, `AccountService`, the JWT filter, persistence mapping and the after-commit publisher.
- Config: secrets are environment variables (`JWT_SECRET`, `DB_PASSWORD`, `RABBITMQ_PASSWORD`, optional `DB_URL`, `RABBITMQ_HOST`...). For local runs put them in `.env` (git-ignored, see `.env.example`); `spring.config.import` loads it. `JWT_SECRET` must match the users service and the gateway.
- Port `8082`, endpoints under `/api/accounts`. Every endpoint needs a valid JWT (`JwtAuthenticationFilter` → `Caller` principal, injected with `@AuthenticationPrincipal`). Authorization rules live in `AccountService`: customers only see/move their own accounts (foreign accounts answer 404), only admins edit accounts or open accounts for other users. Account numbers are generated server side.
- Money is `BigDecimal` end to end. Deposits/withdrawals lock the row (`findByIdForUpdate`, `PESSIMISTIC_WRITE`).
- Schema is owned by Flyway (`db/migration`); never edit an applied migration, add a new `V<n>__*.sql`. `AccountStatus` is stored by its explicit `value` via `AccountStatusConverter`.
- Audit events (`AccountEvent`) are sent as JSON to RabbitMQ (`app.audit.*`) after the transaction commits; consumed by `py-wallet-audit`.
- Errors are RFC 9457 `ProblemDetail` JSON (`GlobalExceptionHandler`); user-facing messages are in Spanish, logs in English.
