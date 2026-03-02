# Tech Stack Documentation
## Video Streaming Platform — Microservices Backend

**Version:** 1.0  
**Date:** March 2026  
**Architecture:** Microservices — REST API Backend  
**Deployment:** Docker / Docker Compose

---

## Table of Contents

1. [Programming Languages](#1-programming-languages)
2. [Backend Framework](#2-backend-framework)
3. [Frontend Framework](#3-frontend-framework)
4. [Database](#4-database)
5. [Authentication & Security](#5-authentication--security)
6. [API Architecture](#6-api-architecture)
7. [Libraries & Tools](#7-libraries--tools)
8. [DevOps & Deployment Tools](#8-devops--deployment-tools)
9. [Testing Tools](#9-testing-tools)
10. [Version Control](#10-version-control)
11. [Monitoring & Logging](#11-monitoring--logging)
12. [Development Environment](#12-development-environment)

---

## 1. Programming Languages

| Language | Version | Usage |
|----------|---------|-------|
| **Java** | 17+ (LTS recommended) | Primary language for all microservices |
| **YAML** | — | Service configuration files (Spring Cloud Config) |
| **Properties** | — | Alternative format for Spring configuration files |
| **JSON** | — | API request/response payload format |

> Java 17+ is recommended for its LTS support, modern language features (records, sealed classes), and compatibility with the Spring Boot 3.x ecosystem.

---

## 2. Backend Framework

### Core Framework

| Technology | Version | Role |
|-----------|---------|------|
| **Spring Boot** | 3.x | Base framework for all microservices |
| **Spring Web (MVC)** | Included in Boot | REST controller layer |
| **Spring Data JPA** | Included in Boot | ORM and data access layer |
| **Spring Validation** | Included in Boot | Request payload validation (`@Valid`, `@NotNull`, etc.) |

### Spring Cloud Components

| Component | Role |
|-----------|------|
| **Spring Cloud Netflix Eureka Server** | Service registry and discovery server (`discovery-service`) |
| **Spring Cloud Netflix Eureka Client** | Client-side registration in each microservice |
| **Spring Cloud Config Server** | Centralized configuration server backed by a Git repository (`config-service`) |
| **Spring Cloud Config Client** | Enables each service to pull config from the Config Server |
| **Spring Cloud Gateway** | API Gateway — single entry point, routing, and filter chain (`gateway-service`) |
| **OpenFeign (Spring Cloud OpenFeign)** | Declarative REST client for inter-service communication (user-service → video-service) |

---

## 3. Frontend Framework

> **Not Applicable.**
>
> This project is **100% backend**. There is no frontend framework, no UI, and no web interface. All API interactions are performed exclusively through **Postman** or equivalent REST API clients.

---

## 4. Database

### Strategy: Database per Service

Each microservice maintains its own **isolated relational database**, in strict adherence to the microservices data isolation principle.

| Service | Database | Schema Purpose |
|---------|----------|----------------|
| `video-service` | Relational DB (choice of team) | Stores `Video` entities |
| `user-service` | Relational DB (choice of team) | Stores `User`, `Watchlist`, `WatchHistory` entities |

### Recommended Relational Database Options

| Database | Notes |
|----------|-------|
| **PostgreSQL** | Recommended — robust, feature-rich, production-grade open-source RDBMS |
| **MySQL / MariaDB** | Widely used, good Spring/JPA compatibility |
| **H2** | In-memory only — suitable for local development and unit testing |

### ORM & Data Access

| Technology | Role |
|-----------|------|
| **Hibernate** | JPA implementation (auto-configured by Spring Data JPA) |
| **Spring Data JPA** | Repository abstraction, query generation, and entity lifecycle management |
| **Flyway / Liquibase** *(recommended)* | Database schema versioning and migration management |

### Core Entities

**Video Service:**
```
Video: id, title, description, thumbnailUrl, trailerUrl (YouTube embed),
       duration, releaseYear, type (FILM/SERIE), category, rating, director, cast
```

**User Service:**
```
User:         id, username, email, password
Watchlist:    id, userId, videoId, addedAt
WatchHistory: id, userId, videoId, watchedAt, progressTime, completed
```

---

## 5. Authentication & Security

### Baseline Security (Core Scope)

| Technology | Role |
|-----------|------|
| **BCrypt** | Password hashing before persistence |
| **Spring Validation** | Server-side input validation on all API endpoints |
| **HTTPS / TLS** *(recommended)* | Encrypt traffic in transit, especially in production |

### Bonus — Dedicated Auth Microservice

| Technology | Role |
|-----------|------|
| **JWT (JSON Web Tokens)** | Stateless authentication tokens issued upon login |
| **Spring Security** | Security filter chain, endpoint protection |
| **Auth Microservice** | Dedicated service for login, token issuance, and validation |
| **Spring Cloud Gateway (filter)** | Token validation at the gateway before routing to downstream services |

#### JWT Flow (Bonus Architecture)
```
Client → Gateway → Auth Service (validate token)
                 ↓ (if valid)
              Downstream Service (User/Video)
```

---

## 6. API Architecture

### Style
- **RESTful API** following HTTP conventions (`GET`, `POST`, `PUT`, `DELETE`)
- **JSON** as the exclusive data interchange format
- **Stateless** request handling — no server-side session state

### Design Patterns (Layered Architecture)

```
┌──────────────────────────────────┐
│         REST Controller          │  ← Receives HTTP requests, returns responses
├──────────────────────────────────┤
│           Service Layer          │  ← Business logic, orchestration
├──────────────────────────────────┤
│         Repository Layer         │  ← Data access (Spring Data JPA)
├──────────────────────────────────┤
│         Database (per service)   │  ← Persistent storage
└──────────────────────────────────┘
```

| Pattern | Implementation |
|---------|---------------|
| **Repository Pattern** | `JpaRepository` interfaces per entity |
| **DTO Pattern** | Separate request/response objects decoupled from entities |
| **Mapper Pattern** | MapStruct or ModelMapper for entity ↔ DTO conversion |

### External API Integration

| API | Purpose |
|-----|---------|
| **TMDb (The Movie Database API)** | Fetch video metadata: title, description, cast, director, ratings, thumbnails |
| **OMDb API** *(alternative)* | Open Movie Database — alternative metadata source |

### Inter-Service Communication

| Method | Technology | Services |
|--------|-----------|----------|
| Synchronous REST | **OpenFeign** | `user-service` → `video-service` |

---

## 7. Libraries & Tools

### Mapping

| Library | Version | Role |
|---------|---------|------|
| **MapStruct** | 1.5+ | Compile-time DTO ↔ Entity mapping (recommended for performance) |
| **ModelMapper** | 3.x | Alternative runtime-based object mapping |

### HTTP Client & Integration

| Library | Role |
|---------|------|
| **Spring Cloud OpenFeign** | Declarative REST client for Feign-based inter-service calls |
| **OkHttp / RestTemplate / WebClient** | HTTP client for TMDb/OMDb external API calls |

### Utility

| Library | Role |
|---------|------|
| **Lombok** | Reduces boilerplate (getters, setters, builders, constructors) |
| **Jackson** | JSON serialization/deserialization (auto-configured by Spring Boot) |
| **Slf4j + Logback** | Standard logging facade and implementation |

### API Documentation *(Recommended)*

| Tool | Role |
|------|------|
| **SpringDoc OpenAPI (Swagger UI)** | Auto-generates interactive API documentation from annotations |

### Resilience *(Recommended)*

| Tool | Role |
|------|------|
| **Resilience4j** | Circuit breaker, retry, rate limiter for Feign clients and service calls |

---

## 8. DevOps & Deployment Tools

### Containerization

| Tool | Role |
|------|------|
| **Docker** | Build container images for each microservice |
| **Docker Compose** | Define and run all services together in a local environment |
| **Dockerfile** | Per-service image definition (base image: `eclipse-temurin:17-jdk-alpine` recommended) |

### Docker Compose Architecture

```yaml
# Services orchestrated by Docker Compose:
services:
  config-service       # Must start first
  discovery-service    # Depends on config-service
  gateway-service      # Depends on discovery-service
  video-service        # Depends on discovery-service + video-db
  user-service         # Depends on discovery-service + user-db
  video-db             # PostgreSQL instance for video-service
  user-db              # PostgreSQL instance for user-service
```

### Build Tool

| Tool | Role |
|------|------|
| **Apache Maven** | Project build, dependency management, packaging |
| **Gradle** *(alternative)* | Alternative build system with Groovy/Kotlin DSL |

### Project Management

| Tool | Role |
|------|------|
| **Jira** | Agile project management, sprint planning, backlog tracking |
| **Git** | Source code version control |

---

## 9. Testing Tools

### API Testing

| Tool | Role |
|------|------|
| **Postman** | Primary tool for manual API testing, collection management, and environment variable configuration |

### Unit & Integration Testing

| Tool | Role |
|------|------|
| **JUnit 5** | Unit test framework |
| **Mockito** | Mocking framework for isolating units under test |
| **Spring Boot Test** | Integration testing with `@SpringBootTest`, `@WebMvcTest`, `@DataJpaTest` |
| **MockMvc** | Testing REST controllers without deploying a full server |
| **H2 (In-Memory DB)** | Lightweight in-memory database for test environments |

### Code Coverage

| Tool | Role |
|------|------|
| **JaCoCo** | Java code coverage reporting integrated with Maven/Gradle |

### Testing Strategy

| Test Type | Scope | Tool |
|-----------|-------|------|
| Unit Tests | Service layer, mapper, utility classes | JUnit 5 + Mockito |
| Controller Tests | REST endpoints | MockMvc + `@WebMvcTest` |
| Repository Tests | JPA queries | `@DataJpaTest` + H2 |
| Integration Tests | Full service context | `@SpringBootTest` |
| API Tests | End-to-end REST flows | Postman Collections |

---

## 10. Version Control

| Tool / Practice | Details |
|----------------|---------|
| **Git** | Distributed version control |
| **GitHub / GitLab / Bitbucket** | Remote repository hosting (team's choice) |
| **Branching Strategy** | Gitflow recommended: `main`, `develop`, `feature/*`, `hotfix/*` |
| **Commit Convention** | Conventional Commits (`feat:`, `fix:`, `chore:`, `docs:`) |
| **Pull Requests** | Mandatory code review before merge to `develop` or `main` |
| **Config Repository** | Separate Git repository for Spring Cloud Config YAML/Properties files |

### Repository Structure (Recommended Monorepo Layout)

```
video-streaming-platform/
├── config-service/
├── discovery-service/
├── gateway-service/
├── video-service/
├── user-service/
├── config-repo/              ← Separate Git repo for Config Server
│   ├── application.yml
│   ├── video-service.yml
│   └── user-service.yml
├── docker-compose.yml
└── README.md
```

---

## 11. Monitoring & Logging

### Baseline Logging (Core Scope)

| Tool | Role |
|------|------|
| **SLF4J + Logback** | Structured logging within each service |
| **Spring Boot Actuator** | Exposes health, metrics, and info endpoints (`/actuator/health`) |

### Bonus — Dedicated Monitoring Microservice

| Tool | Role |
|------|------|
| **Micrometer** | Application metrics facade (integrates with Actuator) |
| **Prometheus** | Metrics scraping and time-series storage |
| **Grafana** | Metrics visualization and dashboards |
| **Spring Boot Admin** *(optional)* | Web UI for managing and monitoring Spring Boot applications |
| **Zipkin / Jaeger** *(optional)* | Distributed tracing for inter-service request flows |
| **ELK Stack** *(optional)* | Elasticsearch + Logstash + Kibana for centralized log aggregation |

### Health Check Endpoints (per service)

```
GET /actuator/health       → Service health status
GET /actuator/info         → Build and version metadata
GET /actuator/metrics      → JVM and application metrics
GET /actuator/env          → Environment and config values
```

---

## 12. Development Environment

### Required Tools

| Tool | Version | Purpose |
|------|---------|---------|
| **JDK** | 17+ (LTS) | Java runtime and compiler |
| **Apache Maven** | 3.8+ | Build and dependency management |
| **Docker Desktop** | Latest | Container runtime |
| **Docker Compose** | 2.x+ | Multi-container orchestration |
| **IntelliJ IDEA** *(recommended)* | Latest | IDE with Spring Boot and Docker support |
| **Postman** | Latest | API testing and collection management |
| **Git** | 2.x+ | Version control |

### IDE Plugins (IntelliJ IDEA)

| Plugin | Purpose |
|--------|---------|
| **Spring Boot** | Spring-specific support, run configurations |
| **Lombok** | Annotation processing support |
| **MapStruct Support** | DTO mapping generation |
| **Docker** | Dockerfile and Compose management inside IDE |
| **SonarLint** | Real-time code quality and bug detection |

### Local Environment Setup Order

```
1. Start config-service        (fetches from Git config repo)
2. Start discovery-service     (registers with Eureka)
3. Start video-db + user-db    (PostgreSQL containers)
4. Start video-service         (registers, pulls config)
5. Start user-service          (registers, pulls config, connects to video-service via Feign)
6. Start gateway-service       (routes traffic to registered services)
```

> Using **Docker Compose**, the entire stack can be started with a single command:
> ```bash
> docker-compose up --build
> ```

---

## Tech Stack Summary

| Layer | Technology |
|-------|-----------|
| Language | Java 17+ |
| Core Framework | Spring Boot 3.x |
| Cloud Components | Spring Cloud (Eureka, Config, Gateway, OpenFeign) |
| ORM | Spring Data JPA / Hibernate |
| Database | PostgreSQL (or MySQL) — one per service |
| Mapping | MapStruct / ModelMapper |
| Build Tool | Apache Maven |
| Containerization | Docker + Docker Compose |
| API Testing | Postman |
| Unit Testing | JUnit 5 + Mockito + MockMvc |
| External API | TMDb API / OMDb API |
| Logging | SLF4J + Logback |
| Monitoring | Spring Boot Actuator + Prometheus + Grafana *(bonus)* |
| Auth | JWT + Spring Security *(bonus)* |
| Version Control | Git (GitHub / GitLab) |
| Project Management | Jira (Agile/Scrum) |

---

*This document reflects the technology choices aligned with the project brief. Teams may substitute equivalent technologies where justified, provided the architectural constraints (microservices, REST, Docker, Spring Cloud) are respected.*
