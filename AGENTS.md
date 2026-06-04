# ticketing-api

## Rules
- README.md, API docs → English
- Client-visible error/validation messages → French
- Never read or commit `.env.properties`
- Never modify files without user validation — always propose first, wait for approval
- After each proposal, always offer exactly 3 choices: "Affiner", "Refuser", "Valider entièrement"

## Stack
- Spring Boot 3.5.4 / Java 21 / PostgreSQL / JWT (jjwt 0.11.5)
- Tests: JUnit 5 + Mockito (no integration test DB, everything mocked)
- Use `@MockitoBean` (Spring Boot 3.5+, not `@MockBean`)

