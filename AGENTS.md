# AGENTS.md

## Project

RESTful helpdesk ticketing API for Loïck CHERIMONT, backend Java/Spring Boot developer (junior in Spring Boot, 7 years of web dev experience). **Spring Boot 3.5.4 / Java 21 / PostgreSQL / JWT (jjwt 0.11.5)**, deployed on **Railway** (prod) with **Supabase** PostgreSQL and Flyway migrations. Frontend is a separate repo (`ticketing-web-v2`, GitHub Pages + Angular).

Language rules:
- **README.md and API docs → English**
- **Client-visible error/validation messages → French**
- Code comments and JavaDoc → English
- Never read or commit `.env.properties`

## Stack quirks

- **`JAVA_HOME` on this machine points to Java 25** → JaCoCo 0.8.12 cannot instrument its bytecode (`Unsupported class file major version 69`). Always build/test with `JAVA_HOME=/usr/lib/jvm/java-21-openjdk`.
- Layered package structure: `controllers` → `services` (interface) → `services/impl` → `repository`; plus `dto/`, `models/`, `filter/`, `configuration/`, `exceptions/`.
- **Never return JPA entities from controllers**: `Ticket.createdBy`/`assignedTo` are `@ManyToOne(fetch = FetchType.LAZY)` and Jackson crashes on the lazy proxy (`HttpMessageConversionException`) → serialize via `TicketResponseDto` (record with `status`, `createdByEmail`, `assignedToEmail`), mapped by the static factory `TicketResponseDto.from(Ticket)` (ternary guard: `assignedTo` is nullable, `createdBy` not).
- Integration tests run through **surefire** (pom includes `**/*IT.java`, there is no failsafe plugin): plain `mvn test` already executes `SecurityIT` (real Postgres via Testcontainers, `postgres:16` bean in `TestcontainersConfiguration`) → **Docker is required**.
- `spring.config.import=optional:file:.env.properties` loads local secrets; `.dockerignore` excludes it so it never lands in the Docker image.
- Flyway: `db/migration` (V1/V2 schema), dev profile seeds demo data from `db/dev-seed`; `ddl-auto=none`.
- pom enforces a **JaCoCo 45% line-coverage gate on `verify`**; `dto/*` and `configuration/*` are excluded from the threshold.

## Coding conventions

- JPA entities live in `models/` and are never exposed over HTTP — always map to a `dto/` record first.
- Controllers are thin: HTTP + validation only, business logic in services, OpenAPI annotations (`@Operation`, `@ApiResponse`, `@Schema`) on every endpoint/DTO.
- Services: interface in `services/` (`TicketService`), implementation in `services/impl/*` — constructor injection with Lombok `@RequiredArgsConstructor`, no field injection.
- Guard role checks with `Objects.requireNonNull(currentUser.getRole())`; agent-only methods annotated `@PreAuthorize("hasRole('AGENT')")`.
- Records (Java 21) for DTOs with a static `from(entity)` factory so tests build expected values with the production mapper.

You are an expert in Java, Spring Boot, and scalable backend development. You write functional, maintainable, secure, and tested code following Spring Boot and Java best practices.

### Java / Spring Boot

- Services first, then controllers — keep controllers free of business logic
- Use `@Valid` + Bean Validation on request DTOs
- Prefer `Optional` + `orElseThrow` over null checks; exception types in `exceptions/`
- Use `@MockitoBean` (Spring Boot 3.5+, not `@MockBean`) in tests

## Commands

- Dev server: `docker compose up -d db` then `SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run` (port 8080, Swagger UI on `/swagger-ui.html`)
- Full build + tests + coverage gate: `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./mvnw clean verify`
- Unit tests only: `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./mvnw -Dtest='*Test' test`
- Docker image: `docker build -t ticketing-api .`
- No linter/format command is configured for Java.

## Testing

- **JUnit 5 + Mockito**, everything mocked at unit level (`*Test` classes in `controllers/` and `services/`).
- `SecurityIT` (`src/test/java/.../SecurityIT.java`) is the only integration test: full Spring context + Testcontainers PostgreSQL 16, verifies a real JWT sign-in flow against `/api/tickets`
- Build expected DTOs with `TicketResponseDto.from(entity)` to keep test assertions in sync with the production mapping.
- Always set `JAVA_HOME` to Java 21 or tests fail at JaCoCo instrumentation (see Stack quirks).