# Repository Guidelines

## Project Structure & Module Organization
- Root `pom.xml` is the aggregator for the multi-module build.
- `barrage-api`: Spring Boot entrypoint (`com.hku.BarrageApp`) and REST APIs under `barrage-api/src/main/java/com/hku/barrage/api`.
- `barrage-service`: business logic, configs, and utilities under `barrage-service/src/main/java/com/hku/barrage/service`.
- `barrage-dao`: domain models and DAO interfaces; MyBatis mapper XML files live in `barrage-dao/src/main/resources/mapper`.
- Resources are module-specific under `*/src/main/resources`. Tests (when added) go in `*/src/test/java`.

## Build, Test, and Development Commands
- `mvn clean package`: build all modules from the repository root.
- `mvn -pl barrage-api -am spring-boot:run`: run the API module with required dependencies.
- `mvn test`: run module tests (currently there are no test classes, so this only compiles test sources).

## Coding Style & Naming Conventions
- Java 8, 4-space indentation, braces on the same line as declarations.
- Package naming follows `com.hku.barrage...`.
- Class naming uses PascalCase with role suffixes such as `*Api`, `*Service`, `*Dao`, and `*Constant` (for example, `UserService`, `UserDao`).
- Mapper XML filenames should align with DAO names (for example, `user.xml` for `UserDao`).

## Testing Guidelines
- Place tests in `*/src/test/java` and name classes with the `*Test` suffix.
- If you add tests, add a test dependency such as `spring-boot-starter-test` to the module where they live.

## Configuration & Local Dependencies
- Active profile defaults to `test` in `barrage-service/src/main/resources/application.properties`.
- Local services: MySQL `barrage` database and credentials in `barrage-api/src/main/resources/application-test.properties`, Redis at `127.0.0.1:6379`, RocketMQ at `localhost:9876`.
- Keep secrets out of Git; use per-environment property files or overrides.

## Commit & Pull Request Guidelines
- Git history only shows `Initial commit`, so no established convention. Use short, imperative subjects (example: `Add user follow endpoint`).
- PRs should include a concise summary, test commands/results, and any config/schema/mapper updates.
