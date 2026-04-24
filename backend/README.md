# Chinvat Backend (Multimodule)

This backend is organized as a Maven multimodule project.

## Modules

- `common` - shared contracts and common types
- `app` - Spring Boot runtime application

## Build and Test

```bash
cd /Users/jiriposavad/Documents/FullStack/chinvat-malaga-2026/backend
./mvnw test
```

## Code Style

- Java style: Google Java Style (Spotless + google-java-format)
- Comments policy: minimalist comments

```bash
cd /Users/jiriposavad/Documents/FullStack/chinvat-malaga-2026/backend
./mvnw spotless:apply
./mvnw spotless:check
```

See `STYLE_GUIDE.md` for details.

## CI

GitHub Actions runs `spotless:check` and `test` for changes under `backend/**`.

## Run Application

```bash
cd /Users/jiriposavad/Documents/FullStack/chinvat-malaga-2026/backend
./mvnw -pl app spring-boot:run
```

## Profiles

- default profile: `local` (from `SPRING_PROFILES_ACTIVE`, fallback `local`)
- available profiles: `local`, `dev`, `prod`

## Environment Variables

Supported variables (examples):

- `SPRING_PROFILES_ACTIVE`
- `SERVER_PORT`
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JPA_DDL_AUTO`
- `JPA_SHOW_SQL`
- `H2_CONSOLE_ENABLED`

Example local run with env vars:

```bash
cd /Users/jiriposavad/Documents/FullStack/chinvat-malaga-2026/backend
SPRING_PROFILES_ACTIVE=local DB_USERNAME=sa ./mvnw -pl app spring-boot:run
```

