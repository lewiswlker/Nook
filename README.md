# Nook

A modular Spring Boot backend for user, content, and media features, built with MyBatis and Redis.

## Features
- REST APIs for users, auth, follow relationships, moments, and video
- MyBatis-based persistence layer with XML mappers
- Redis integration for caching and tokens
- RocketMQ support for async messaging

## Tech Stack
- Java 8
- Spring Boot 2.5.1
- MyBatis 2.3.1
- MySQL 8
- Redis, RocketMQ

## Project Structure
- `nook-api`: Spring Boot entrypoint and REST APIs
- `nook-service`: business logic, configs, utilities
- `nook-dao`: domain models and DAO interfaces (mapper XMLs in `src/main/resources/mapper`)

## Requirements
- JDK 8
- Maven 3.6+
- MySQL, Redis, RocketMQ (see configuration below)

## Getting Started
Build all modules:
```bash
mvn clean package
```

Run the API module:
```bash
mvn -pl nook-api -am spring-boot:run
```

## Configuration
- Default profile is `test`.
- MySQL and Redis settings live in `nook-api/src/main/resources/application-test.properties`.
- Module-level configs are under each module's `src/main/resources`.

## Notes
- Keep secrets out of source control. Use environment-specific property files or overrides.

## License
Specify your license here.
