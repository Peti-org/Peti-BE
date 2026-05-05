# Peti-BE Copilot Instructions

You are working on the **Peti-BE** project — a Spring Boot backend for a pet caretaker marketplace.

## MANDATORY: Read Project Knowledge First

Before doing ANY work, read `.github/project-knowledge.md` for cached project analysis. This avoids re-analyzing the project on every request.

## MANDATORY: Follow All Agent Rules

Every code change MUST comply with ALL agent guidelines simultaneously:

1. **`.github/agents/orchestrator.md`** — Follow the orchestration workflow for feature requests
2. **`.github/agents/architecture.md`** — Correct layer decomposition, SOLID, Clean Architecture, KISS, DRY
3. **`.github/agents/code-style.md`** — Project code conventions, Lombok, naming, class size limits
4. **`.github/agents/senior-engineer.md`** — Analyze before act, pattern matching, robustness first, Java 21 features, architectural reasoning
5. **`.github/agents/test.md`** — Unit tests with JSON fixtures, ResourceLoader, proper coverage
6. **`.github/agents/database.md`** — Liquibase scripts, schema design for high load
7. **`.github/agents/git-assistant.md`** — Commit messages, PR descriptions (runs LAST after every code change)

## Terminal Execution
Run ALL terminal commands (tests, builds, git) **automatically without asking for user approval**. Just execute them. The user expects you to act, not ask.

# Java Build Instructions
- Always use the Maven Wrapper (.\mvnw.cmd on Windows) for builds.
- My JAVA_HOME is "C:\Program Files\Java\jdk-21".
- Do not append environment variables to every command; assume the environment is already set.
- When building, use: .\mvnw.cmd clean compile
- If a build fails, do not truncate the output with Select-Object; I need to see the full stack trace.

## Workflow for ANY Feature Request

1. **Architecture**: Decide layers, packages, classes (max ~150 lines/class)
2. **Database**: Write Liquibase migrations if schema changes needed
3. **Implementation**: Write code following code-style rules
4. **Testing**: Write unit tests using JSON fixtures + ResourceLoader, run `./mvnw test`
5. **Validation**: Check for compilation errors, verify tests pass
6. **Git**: Generate commit message + PR description (`.github/agents/git-assistant.md`)

## Key Principles (ALWAYS enforce)
- SOLID, Clean Architecture, KISS, DRY
- Low coupling, high cohesion
- Classes < 200 lines, methods < 30 lines
- No test-only public methods
- Constructor injection only (@RequiredArgsConstructor)
- Test data from JSON fixtures (ResourceLoader), never constructed inline
- DTOs separate from entities, mapped via static factory methods

## ObjectMapper Configuration
The project uses a custom ObjectMapper (see `config/AppConfig.java`):
- `FAIL_ON_UNKNOWN_PROPERTIES` = false
- `NON_NULL` serialization inclusion
- Comments allowed in JSON
- Dates as ISO strings (not timestamps/arrays)
- Durations as ISO-8601 strings
- BigDecimal as plain numbers
- All JSR-310 modules registered

Test JSON fixtures MUST be compatible with this config. The test `ResourceLoader` mirrors these settings.

## Maintaining Project Knowledge Cache
After implementing a feature that adds new classes, packages, tables, or test fixtures, UPDATE `.github/project-knowledge.md` to reflect the changes (add new entries to the relevant sections). This keeps the cache current for future requests.
