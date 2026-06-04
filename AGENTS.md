# ticketing-api

## Learning
- User is **junior in Java/Spring Boot** but has 7 years of web dev experience — don't explain web fundamentals (HTTP, REST, API, DB), only Spring Boot / Java specific concepts
- Leverage web dev analogies to explain Spring Boot patterns (e.g. "a filter is like Express middleware")
- Never give the answer directly — guide with questions, let the user propose their solution first
- When the user asks "why" about an approach, explain the **intent** first, then the mechanics

## Rules
- README.md, API docs → English
- Client-visible error/validation messages → French
- Never read or commit `.env.properties`
- Always propose first without modify, wait for user validation
- After each proposal, always offer exactly 3 choices: "1. Valider", "2. Refuser", "3. Autre - Fournir les détails"
    - If "1. Valider" : apply the suggestion into the project
    - Else "2. Refuser" : forget the suggestion
    - Else if "3. Autre - Fournir les détails" : ask user more informations

## Stack
- Spring Boot 3.5.4 / Java 21 / PostgreSQL / JWT (jjwt 0.11.5)
- Tests: JUnit 5 + Mockito (no integration test DB, everything mocked)
- Use `@MockitoBean` (Spring Boot 3.5+, not `@MockBean`)

