# Nook

![Java](https://img.shields.io/badge/Java-8-informational)
![Spring%20Boot](https://img.shields.io/badge/Spring%20Boot-2.5.1-brightgreen)
![Vue](https://img.shields.io/badge/Vue-2.x-4FC08D)
![MySQL](https://img.shields.io/badge/MySQL-8.x-4479A1)
![Redis](https://img.shields.io/badge/Redis-6379-D82C20)
![RocketMQ](https://img.shields.io/badge/RocketMQ-4.9.4-FF6A00)
![Elasticsearch](https://img.shields.io/badge/Elasticsearch-7.12.1-005571)

English | [简体中文](README.zh-CN.md)

Nook is a full-stack video and social platform built with Spring Boot and a Vue 2 frontend. It includes user/auth flows, follow groups, moments, video management, danmu (bullet chat), file uploads, and optional AI chat/search features.

## Features
- User registration/login and role-based access control
- Follow groups, user moments, and video-related features
- Danmu (bullet chat) and WebSocket support
- File uploads and FastDFS integration
- Redis caching and token storage
- RocketMQ messaging and Elasticsearch search services
- Optional AI chat with tool calling and web search

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
- Zhipu AI SDK (`ai.z.openapi:zai-sdk`)
- Recommendation: Apache Mahout (`org.apache.mahout:mahout-mr`)

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
- Backend properties live in `nook-api/src/main/resources/application-test.properties`.
- Local overrides can be added to `nook-api/src/main/resources/application-local.properties` (ignored by Git).
- Frontend env files are `nook-web/.env.development` and `nook-web/.env.production`.

## Get Started
Backend (from the repository root):
```bash
mvn clean package
mvn -pl nook-api -am spring-boot:run
```

Frontend (from `nook-web`):
```bash
npm install
npm run serve
```

Notes:
- The API runs on port `8080` by default (Spring Boot default).
- If `8080` is in use, run the frontend on another port: `npm run serve -- --port 8081`.
- The frontend reads the API base URL from `localStorage.baseApi` and falls back to `http://localhost:8080` (`nook-web/config/env.js`). You can also set `VUE_APP_BASE_URL` in `.env.*`.
- Build for production: `npm run build`.

## AI Features
Nook includes an optional AI assistant with streaming chat and tool calling.

- Model: `glm-4.5-air` (configurable via `ai.zhipu.model`)
- Provider: Zhipu AI SDK (`zai-sdk`)
- Runtime: JDK 8 (same as backend)
- Web search: Tavily (optional)

Configuration keys (backend):
- `ai.zhipu.api-key`: Zhipu API key
- `ai.zhipu.model`: model name (default `glm-4.5-air`)
- `ai.tavily.api-key`: Tavily API key (can be set via `TAVILY_API_KEY`)
- `ai.tavily.search-depth`, `ai.tavily.max-results`

The streaming endpoint is exposed at `/ai/chat/stream` (see `nook-api/src/test/java/com/hku/nook/api/StreamChatManual.java`).

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

Note: The frontend and AI features are still under active optimization.
