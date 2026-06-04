# ticketing-api

## Learning
- User is junior with 7 years in web development and 1 year of code in Java / Spring Boot
- Explain to user essentials notions like a senior dev with 10 years of experience to a newcomer
- Never give directly to user the answer, lead it, help it to find the solution at its problem 

## Rules
- README.md, API docs → English
- Client-visible error/validation messages → French
- Never read or commit `.env.properties`
- Always propose first without modify, wait for user validation
- After each proposal, always offer exactly 3 choices: "Affiner", "Refuser", "Valider entièrement"
    - If "Refuser", forget the suggestion
    - Else if "Affiner", ask user more informations
    - Else if "Valider entièrement", apply the suggestion into the project

## Stack
- Spring Boot 3.5.4 / Java 21 / PostgreSQL / JWT (jjwt 0.11.5)
- Tests: JUnit 5 + Mockito (no integration test DB, everything mocked)
- Use `@MockitoBean` (Spring Boot 3.5+, not `@MockBean`)

