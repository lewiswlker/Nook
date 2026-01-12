# Nook

![Java](https://img.shields.io/badge/Java-8-informational)
![Spring%20Boot](https://img.shields.io/badge/Spring%20Boot-2.5.1-brightgreen)
![Vue](https://img.shields.io/badge/Vue-2.x-4FC08D)
![MySQL](https://img.shields.io/badge/MySQL-8.x-4479A1)
![Redis](https://img.shields.io/badge/Redis-6379-D82C20)
![RocketMQ](https://img.shields.io/badge/RocketMQ-4.9.4-FF6A00)
![Elasticsearch](https://img.shields.io/badge/Elasticsearch-7.12.1-005571)

[English](README.md) | 简体中文

Nook 是一个全栈视频与社交平台，后端基于 Spring Boot，前端基于 Vue 2。包含用户/权限、关注分组、动态、视频、弹幕、文件上传，以及可选的 AI 对话与搜索能力。

## 功能特性
- 用户注册登录与角色权限控制
- 关注分组、用户动态与视频相关能力
- 弹幕与 WebSocket 支持
- 文件上传与 FastDFS 集成
- Redis 缓存与 Token
- RocketMQ 消息与 Elasticsearch 搜索
- 可选 AI 对话（工具调用与联网搜索）

## 项目结构
- `nook-api`: Spring Boot 启动入口与 REST API
- `nook-service`: 业务逻辑、配置与工具类
- `nook-dao`: 领域模型、DAO 与 MyBatis Mapper
- `nook-web`: Vue 2 前端

## 技术栈
后端
- Java 8, Spring Boot 2.5.1
- MyBatis 2.3.1, MySQL 8
- Redis, RocketMQ, Elasticsearch
- FastDFS
- 智谱 AI SDK (`ai.z.openapi:zai-sdk`)
- 推荐系统：Apache Mahout (`org.apache.mahout:mahout-mr`)

前端
- Vue 2, Vue Router, Vuex
- Element-UI

## 环境要求
- JDK 8
- Maven 3.6+
- Node.js 14+ 与 npm
- MySQL, Redis, RocketMQ
- 可选：Elasticsearch, FastDFS（见 `docker-compose.yml`）

## 配置说明
- 默认 Spring Profile 为 `test`（见 `nook-service/src/main/resources/application.properties`）。
- 后端配置位于 `nook-api/src/main/resources/application-test.properties`。
- 本地覆盖配置可放在 `nook-api/src/main/resources/application-local.properties`（已忽略）。
- 前端环境变量文件在 `nook-web/.env.development` 与 `nook-web/.env.production`。

## Get Started
后端（在仓库根目录执行）：
```bash
mvn clean package
mvn -pl nook-api -am spring-boot:run
```

前端（进入 `nook-web` 目录）：
```bash
npm install
npm run serve
```

说明：
- 默认端口为 `8080`（Spring Boot 默认）。
- 如果 `8080` 被占用，可以为前端指定新端口：`npm run serve -- --port 8081`。
- 前端会读取 `localStorage.baseApi`，默认回落到 `http://localhost:8080`（见 `nook-web/config/env.js`）。也可以在 `.env.*` 中设置 `VUE_APP_BASE_URL`。
- 生产构建：`npm run build`。

## AI 功能
Nook 提供可选 AI 助手，支持流式对话与工具调用。

- 模型：`glm-4.5-air`（通过 `ai.zhipu.model` 配置）
- 服务：智谱 AI SDK（`zai-sdk`）
- 运行环境：JDK 8（与后端一致）
- 联网搜索：Tavily（可选）

相关配置（后端）：
- `ai.zhipu.api-key`：智谱 API Key
- `ai.zhipu.model`：模型名称（默认 `glm-4.5-air`）
- `ai.tavily.api-key`：Tavily API Key（可通过 `TAVILY_API_KEY` 设置）
- `ai.tavily.search-depth`, `ai.tavily.max-results`

流式接口示例：`/ai/chat/stream`（见 `nook-api/src/test/java/com/hku/nook/api/StreamChatManual.java`）。

## 数据库
- SQL 脚本位于 `nook-dao/src/main/resources/sql`。
- 创建 `nook` 数据库并执行脚本初始化表结构。

## 本地依赖服务
后端默认依赖以下本地服务：
- MySQL: `localhost:3306`（数据库 `nook`）
- Redis: `127.0.0.1:6379`
- RocketMQ: `localhost:9876`
- Elasticsearch: `127.0.0.1:9200`
- FastDFS tracker/storage（见 `docker-compose.yml`）

可使用 Docker Compose 启动 Elasticsearch/Kibana 与 FastDFS：
```bash
docker compose up -d
```

## 常用脚本
后端
- `mvn clean package` - 构建全部模块
- `mvn test` - 编译测试源码（当前无测试）

前端（在 `nook-web` 目录）
- `npm run serve` - 本地开发
- `npm run build` - 生产构建
- `npm run lint` - 代码检查

说明：前端与 AI 功能仍在持续优化中。
