# Nook

Nook is a full-stack video and social platform built with Spring Boot and a Vue 2 frontend. It provides user/auth flows, follow groups, moments, video, danmu (bullet chat), file uploads, and optional AI chat/search features.

## Table of Contents
- [Features](#features)
- [Project Structure](#project-structure)
- [Tech Stack](#tech-stack)
- [Requirements](#requirements)
- [Configuration](#configuration)
- [Backend: Build and Run](#backend-build-and-run)
- [Frontend: Build and Run](#frontend-build-and-run)
- [Database](#database)
- [Local Dependencies](#local-dependencies)
- [Scripts](#scripts)
- [License](#license)

## Features
- User registration/login and role-based access control
- Follow groups, user moments, and video-related features
- Danmu (bullet chat) and WebSocket support
- File uploads and FastDFS integration
- Redis caching and token storage
- RocketMQ messaging and Elasticsearch search services
- Optional AI chat/search integrations

## Project Structure
- `nook-api`: Spring Boot entrypoint and REST APIs
- `nook-service`: business logic, configs, utilities
- `nook-dao`: domain models, DAO interfaces, MyBatis mappers
- `nook-web`: Vue 2 frontend

## Tech Stack
Backend
- Java 8, Spring Boot 2.5.1
- MyBatis 2.3.1, MySQL 8
- Redis, RocketMQ, Elasticsearch
- FastDFS

Frontend
- Vue 2, Vue Router, Vuex
- Element-UI

## Requirements
- JDK 8
- Maven 3.6+
- Node.js 14+ and npm
- MySQL, Redis, RocketMQ
- Optional: Elasticsearch, FastDFS (see `docker-compose.yml`)

## Configuration
- Default Spring profile is `test` (see `nook-service/src/main/resources/application.properties`).
- Backend properties are in `nook-api/src/main/resources/application-test.properties`.
- Local overrides can be added to `nook-api/src/main/resources/application-local.properties` (ignored by Git).
- Frontend env files live in `nook-web/.env.development` and `nook-web/.env.production`.

## Backend: Build and Run
From the repository root:
```bash
mvn clean package
mvn -pl nook-api -am spring-boot:run
```

The API runs on port `8080` by default (Spring Boot default). Update the port in your Spring configuration if needed.

## Frontend: Build and Run
From `nook-web`:
```bash
npm install
npm run serve
```

By default the frontend reads the API base URL from `localStorage.baseApi` and falls back to `http://localhost:8080` (`nook-web/config/env.js`). If your backend runs elsewhere, update `localStorage.baseApi` or set `VUE_APP_BASE_URL` in the `.env.*` files.

Build for production:
```bash
npm run build
```

## Database
- Schema SQL files are under `nook-dao/src/main/resources/sql`.
- Create a `nook` database and run the SQL scripts to initialize tables.

## Local Dependencies
The backend expects local services by default:
- MySQL: `localhost:3306` (db `nook`)
- Redis: `127.0.0.1:6379`
- RocketMQ: `localhost:9876`
- Elasticsearch: `127.0.0.1:9200`
- FastDFS tracker/storage (see `docker-compose.yml`)

You can bring up Elasticsearch/Kibana and FastDFS via Docker Compose:
```bash
docker compose up -d
```

## Scripts
Backend
- `mvn clean package` - build all modules
- `mvn test` - compile test sources (no tests yet)

Frontend (from `nook-web`)
- `npm run serve` - dev server
- `npm run build` - production build
- `npm run lint` - lint

## License
Specify your license here.
