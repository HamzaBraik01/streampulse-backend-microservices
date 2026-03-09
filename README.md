# 🎬 StreamPulse — Video Streaming Platform (Backend Microservices)

[![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.0-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2023.0.2-brightgreen?logo=spring)](https://spring.io/projects/spring-cloud)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue?logo=postgresql)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker)](https://docs.docker.com/compose/)
[![License](https://img.shields.io/badge/License-MIT-yellow)]()

**StreamPulse** is a fully backend video streaming platform built on a **microservices architecture** using **Spring Boot** and **Spring Cloud**. It exposes RESTful APIs to manage video content, users, watchlists, and viewing history. The system is designed for scalability, maintainability, and resilience.

> 📌 This is a **100% backend project** — no frontend. All interactions are performed through REST API calls (Postman collection included).

---

## 📑 Table of Contents

- [Architecture Overview](#-architecture-overview)
- [Tech Stack](#-tech-stack)
- [Microservices](#-microservices)
  - [Config Service](#1-config-service)
  - [Discovery Service](#2-discovery-service)
  - [Gateway Service](#3-gateway-service)
  - [Video Service](#4-video-service)
  - [User Service](#5-user-service)
- [Database Schema](#-database-schema)
- [API Endpoints](#-api-endpoints)
- [Authentication & Security](#-authentication--security)
- [Getting Started](#-getting-started)
  - [Prerequisites](#prerequisites)
  - [Run with Docker Compose](#option-1-docker-compose-recommended)
  - [Run Locally](#option-2-run-locally)
- [Environment Variables](#-environment-variables)
- [API Testing with Postman](#-api-testing-with-postman)
- [Project Structure](#-project-structure)
- [Design Patterns](#-design-patterns)
- [Contributing](#-contributing)

---

## 🏗 Architecture Overview

```
                          ┌─────────────────────────┐
                          │      Config Service      │
                          │       (Port 8888)        │
                          │  Git-backed / Native     │
                          └────────────┬────────────┘
                                       │ config
                                       ▼
┌──────────┐         ┌─────────────────────────────────┐
│  Client   │────────▶│       API Gateway               │
│ (Postman) │         │       (Port 8090)               │
└──────────┘         │  JWT Validation · CORS · Routing │
                      └──────┬──────────────┬───────────┘
                             │              │
                    ┌────────▼───┐    ┌─────▼────────┐
                    │   Video    │    │    User       │
                    │  Service   │◀───│   Service     │
                    │ (Port 8081)│    │  (Port 8082)  │
                    └─────┬──────┘    └──────┬────────┘
                          │                  │
                    ┌─────▼──────┐    ┌──────▼────────┐
                    │  video-db  │    │   user-db     │
                    │ PostgreSQL │    │  PostgreSQL   │
                    │ (Port 5432)│    │  (Port 5433)  │
                    └────────────┘    └───────────────┘
                             │              │
                      ┌──────▼──────────────▼──────┐
                      │    Discovery Service        │
                      │    Eureka (Port 8761)       │
                      └─────────────────────────────┘
```

### Communication Flow

1. **All external requests** go through the **API Gateway** (port `8090`)
2. The Gateway **validates JWT tokens** and routes requests to downstream services
3. **User Service** communicates with **Video Service** via **OpenFeign** (synchronous REST)
4. All services **register** with the **Eureka Discovery Service**
5. All services can **pull configuration** from the **Config Service**

---

## 🛠 Tech Stack

| Layer                 | Technology                                     |
|-----------------------|------------------------------------------------|
| **Language**          | Java 17 (LTS)                                  |
| **Framework**         | Spring Boot 3.3.0                              |
| **Cloud**             | Spring Cloud 2023.0.2                          |
| **API Gateway**       | Spring Cloud Gateway                           |
| **Service Discovery** | Netflix Eureka Server/Client                   |
| **Config Management** | Spring Cloud Config Server (Native/Git-backed) |
| **Inter-Service**     | Spring Cloud OpenFeign                         |
| **Security**          | Spring Security + JWT (jjwt 0.12.6)            |
| **Database**          | PostgreSQL 15                                  |
| **ORM**               | Spring Data JPA / Hibernate                    |
| **Mapping**           | MapStruct 1.5.5                                |
| **API Docs**          | SpringDoc OpenAPI (Swagger UI)                 |
| **Resilience**        | Resilience4j (Circuit Breaker)                 |
| **Build Tool**        | Maven                                          |
| **Containerization**  | Docker + Docker Compose                        |
| **External API**      | TMDb (The Movie Database) API                  |

---

## 📦 Microservices

### 1. Config Service

| Property   | Value                          |
|------------|--------------------------------|
| **Port**   | `8888`                         |
| **Role**   | Centralized configuration      |
| **Type**   | Infrastructure                 |

Serves externalized configuration (YAML/Properties) to all services from a local classpath (`config-repo/`) or a remote Git repository. Enables config changes without redeployment.

---

### 2. Discovery Service

| Property   | Value                          |
|------------|--------------------------------|
| **Port**   | `8761`                         |
| **Role**   | Service registry & discovery   |
| **Type**   | Infrastructure                 |

Runs a **Netflix Eureka Server**. All microservices register themselves on startup, enabling dynamic service discovery by logical name instead of hard-coded URLs.

**Dashboard**: `http://localhost:8761`

---

### 3. Gateway Service

| Property   | Value                          |
|------------|--------------------------------|
| **Port**   | `8090` (external) → `8080` (internal) |
| **Role**   | API Gateway, JWT validation, routing   |
| **Type**   | Infrastructure                 |

Single entry point for all external API requests. Routes traffic to downstream services using Eureka-based load balancing (`lb://`).

**Key Features:**
- JWT token validation on secured endpoints
- Header propagation (`X-Auth-Username`, `X-Auth-Role`)
- CORS configuration (all origins allowed)
- Dynamic routing via Eureka discovery

**Routes:**

| Route            | Pattern             | Target Service  |
|------------------|---------------------|-----------------|
| Auth endpoints   | `/api/auth/**`      | `user-service`  |
| Video endpoints  | `/api/videos/**`    | `video-service` |
| User endpoints   | `/api/users/**`     | `user-service`  |

**Open (public) endpoints:** `/api/auth/register`, `/api/auth/login`, `/actuator/**`, `/swagger-ui/**`, `/eureka/**`

---

### 4. Video Service

| Property   | Value                          |
|------------|--------------------------------|
| **Port**   | `8081`                         |
| **Role**   | Video content management       |
| **Database** | `video_db` (PostgreSQL)      |
| **Type**   | Business Service               |

Full CRUD operations for video content. Integrates with **TMDb API** to search and import movie metadata (title, description, cast, director, poster, trailer).

**Features:**
- Create, read, update, delete video entries
- Search videos by title, type, or category
- TMDb integration: search movies and auto-import metadata
- YouTube trailer URL storage (embed links)
- Swagger UI documentation

**Video Types:** `FILM`, `SERIE`

**Video Categories:** `ACTION`, `COMEDIE`, `DRAME`, `SCIENCE_FICTION`, `THRILLER`, `HORREUR`

---

### 5. User Service

| Property   | Value                          |
|------------|--------------------------------|
| **Port**   | `8082`                         |
| **Role**   | User management, auth, watchlist, history |
| **Database** | `user_db` (PostgreSQL)       |
| **Type**   | Business Service               |

Manages user accounts, authentication (JWT), watchlists, and viewing history. Communicates with **Video Service** via **OpenFeign** to validate videos.

**Features:**
- User registration & login with JWT authentication
- BCrypt password hashing
- CRUD operations on user accounts
- Watchlist management (add/remove/list videos)
- Viewing history recording (progress tracking, completion status)
- Viewing statistics (total watched, completion rate, watch time)
- Feign client with fallback factory for video-service resilience

---

## 🗃 Database Schema

### Video Service — `video_db`

```
┌──────────────────────────────────┐
│            videos                │
├──────────────────────────────────┤
│ id           BIGINT (PK, AUTO)  │
│ title        VARCHAR (NOT NULL) │
│ description  TEXT               │
│ thumbnail_url VARCHAR           │
│ trailer_url   VARCHAR           │
│ duration      INTEGER           │
│ release_year  INTEGER           │
│ type          VARCHAR (NOT NULL)│  ← FILM | SERIE
│ category      VARCHAR (NOT NULL)│  ← ACTION | COMEDIE | DRAME | ...
│ rating        DOUBLE            │  ← 0.0 – 10.0
│ director      VARCHAR           │
│ cast          TEXT               │
└──────────────────────────────────┘
```

### User Service — `user_db`

```
┌──────────────────────────────────┐      ┌─────────────────────────────────┐
│            users                 │      │          watchlist              │
├──────────────────────────────────┤      ├─────────────────────────────────┤
│ id        BIGINT (PK, AUTO)     │      │ id        BIGINT (PK, AUTO)    │
│ username  VARCHAR (UNIQUE, NN)  │      │ user_id   BIGINT (NOT NULL)    │
│ email     VARCHAR (UNIQUE, NN)  │      │ video_id  BIGINT (NOT NULL)    │
│ password  VARCHAR (NOT NULL)    │      │ added_at  TIMESTAMP (NOT NULL) │
│ role      VARCHAR (NOT NULL)    │      │ UNIQUE(user_id, video_id)      │
│           default: ROLE_USER    │      └─────────────────────────────────┘
└──────────────────────────────────┘
                                         ┌─────────────────────────────────┐
                                         │       watch_history             │
                                         ├─────────────────────────────────┤
                                         │ id            BIGINT (PK, AUTO)│
                                         │ user_id       BIGINT (NOT NULL)│
                                         │ video_id      BIGINT (NOT NULL)│
                                         │ watched_at    TIMESTAMP (NN)   │
                                         │ progress_time INTEGER          │  ← seconds
                                         │ completed     BOOLEAN (NN)     │  ← default: false
                                         └─────────────────────────────────┘
```

---

## 📡 API Endpoints

> **Base URL**: `http://localhost:8090` (via Gateway)

### 🔐 Authentication

| Method | Endpoint               | Description           | Auth |
|--------|------------------------|-----------------------|------|
| POST   | `/api/auth/register`   | Register a new user   | ❌    |
| POST   | `/api/auth/login`      | Login & get JWT token | ❌    |

### 🎥 Videos

| Method | Endpoint                         | Description                  | Auth |
|--------|----------------------------------|------------------------------|------|
| GET    | `/api/videos`                    | List all videos (with filters) | ✅   |
| GET    | `/api/videos/{id}`               | Get video by ID              | ✅    |
| POST   | `/api/videos`                    | Create a new video           | ✅    |
| PUT    | `/api/videos/{id}`               | Update a video               | ✅    |
| DELETE | `/api/videos/{id}`               | Delete a video               | ✅    |
| GET    | `/api/videos/search?title=`      | Search videos by title       | ✅    |
| GET    | `/api/videos/type/{type}`        | Filter by type (FILM/SERIE)  | ✅    |
| GET    | `/api/videos/category/{category}`| Filter by category           | ✅    |
| GET    | `/api/videos/tmdb/search?title=` | Search TMDb for movies       | ✅    |
| POST   | `/api/videos/tmdb/import/{tmdbId}` | Import movie from TMDb     | ✅    |

### 👤 Users

| Method | Endpoint                              | Description                  | Auth |
|--------|---------------------------------------|------------------------------|------|
| POST   | `/api/users`                          | Create a user                | ✅    |
| GET    | `/api/users/{id}`                     | Get user by ID               | ✅    |
| PUT    | `/api/users/{id}`                     | Update user (partial)        | ✅    |
| DELETE | `/api/users/{id}`                     | Delete user                  | ✅    |

### 📋 Watchlist

| Method | Endpoint                                  | Description                 | Auth |
|--------|-------------------------------------------|-----------------------------|------|
| POST   | `/api/users/{id}/watchlist/{videoId}`     | Add video to watchlist      | ✅    |
| DELETE | `/api/users/{id}/watchlist/{videoId}`     | Remove video from watchlist | ✅    |
| GET    | `/api/users/{id}/watchlist`               | Get user's watchlist        | ✅    |

### 📊 Viewing History & Stats

| Method | Endpoint                              | Description                     | Auth |
|--------|---------------------------------------|---------------------------------|------|
| POST   | `/api/users/{id}/history`             | Record a viewing event          | ✅    |
| GET    | `/api/users/{id}/history`             | Get user's viewing history      | ✅    |
| GET    | `/api/users/{id}/stats`               | Get viewing statistics          | ✅    |

---

## 🔒 Authentication & Security

The platform uses **JWT (JSON Web Tokens)** for stateless authentication.

### Authentication Flow

```
1. Client → POST /api/auth/register  → Creates account, returns JWT
2. Client → POST /api/auth/login     → Authenticates, returns JWT
3. Client → GET /api/videos (+ Bearer token in header) → Gateway validates JWT → Routes to service
```

### JWT Details

| Property        | Value                  |
|-----------------|------------------------|
| **Algorithm**   | HMAC-SHA               |
| **Token Type**  | Bearer                 |
| **Expiration**  | 24 hours (86400000 ms) |
| **Header**      | `Authorization: Bearer <token>` |

### Security Features

- **BCrypt** password hashing before storage
- **JWT validation** at the API Gateway level (global filter)
- **Stateless sessions** — no server-side session storage
- **Role-based** access control (ROLE_USER default)
- **CORS** configured for cross-origin requests
- **Input validation** on all API endpoints (`@Valid`, `@NotBlank`, `@Email`, etc.)

### Registration Request Example

```json
POST /api/auth/register
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "securePass123"
}
```

### Response

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "username": "john_doe",
  "email": "john@example.com",
  "role": "ROLE_USER"
}
```

---

## 🚀 Getting Started

### Prerequisites

- **Java 17+** (JDK)
- **Maven 3.8+**
- **Docker** & **Docker Compose**
- **PostgreSQL 15** (if running locally without Docker)
- **TMDb API Key** (optional — for movie import feature, get one at [themoviedb.org](https://www.themoviedb.org/settings/api))

---

### Option 1: Docker Compose (Recommended)

Start the entire stack with a single command:

```bash
# Clone the repository
git clone <repository-url>
cd streampulse-backend-microservices

# (Optional) Set your TMDb API key
export TMDB_API_KEY=your_tmdb_api_key

# Build and start all services
docker-compose up --build
```

This will start the following services in order:

| Service           | URL                          | Start Order |
|-------------------|------------------------------|-------------|
| video-db          | `localhost:5432`             | 1           |
| user-db           | `localhost:5433`             | 1           |
| config-service    | `http://localhost:8888`      | 2           |
| discovery-service | `http://localhost:8761`      | 3           |
| gateway-service   | `http://localhost:8090`      | 4           |
| video-service     | `http://localhost:8081`      | 4           |
| user-service      | `http://localhost:8082`      | 5           |

> ⏳ Services have health checks configured. Docker Compose ensures proper startup ordering via `depends_on` with `condition: service_healthy`.

To stop all services:

```bash
docker-compose down
```

To stop and remove volumes (database data):

```bash
docker-compose down -v
```

---

### Option 2: Run Locally

#### 1. Start PostgreSQL databases

```sql
-- Create databases
CREATE DATABASE video_db;
CREATE DATABASE user_db;
```

#### 2. Start services in order

```bash
# Terminal 1 — Config Service
cd config-service
./mvnw spring-boot:run

# Terminal 2 — Discovery Service
cd discovery-service
./mvnw spring-boot:run

# Terminal 3 — Gateway Service
cd gateway-service
./mvnw spring-boot:run

# Terminal 4 — Video Service
cd video-service
./mvnw spring-boot:run

# Terminal 5 — User Service
cd user-service
./mvnw spring-boot:run
```

> **Windows**: Use `mvnw.cmd` instead of `./mvnw`

---

## ⚙ Environment Variables

### Video Service

| Variable                | Description                  | Default                               |
|-------------------------|------------------------------|---------------------------------------|
| `SPRING_DATASOURCE_URL` | JDBC URL for video database  | `jdbc:postgresql://127.0.0.1:5432/video_db` |
| `SPRING_DATASOURCE_USERNAME` | Database username       | `postgres`                            |
| `SPRING_DATASOURCE_PASSWORD` | Database password       | `ADMIN`                               |
| `TMDB_API_KEY`          | TMDb API key for movie import | —                                    |
| `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` | Eureka URL  | `http://localhost:8761/eureka/`       |

### User Service

| Variable                | Description                  | Default                               |
|-------------------------|------------------------------|---------------------------------------|
| `SPRING_DATASOURCE_URL` | JDBC URL for user database   | `jdbc:postgresql://127.0.0.1:5432/user_db` |
| `SPRING_DATASOURCE_USERNAME` | Database username       | `postgres`                            |
| `SPRING_DATASOURCE_PASSWORD` | Database password       | `ADMIN`                               |
| `JWT_SECRET`            | Base64-encoded JWT secret key | (set in docker-compose)              |
| `JWT_EXPIRATION`        | JWT token expiration in ms   | `86400000` (24 hours)                 |
| `FEIGN_CLIENT_VIDEO-SERVICE_URL` | Video service URL    | `http://localhost:8081`               |

### Gateway Service

| Variable                | Description                  | Default                               |
|-------------------------|------------------------------|---------------------------------------|
| `JWT_SECRET`            | Base64-encoded JWT secret key (same as user-service) | (set in docker-compose) |
| `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` | Eureka URL  | `http://localhost:8761/eureka/`       |

---

## 🧪 API Testing with Postman

A **Postman collection** is included in the repository:

📁 **`StreamPulse_API.postman_collection.json`**

### Import the collection:

1. Open **Postman**
2. Click **Import** → Select the JSON file
3. The collection includes all endpoints organized by service

### Workflow:

1. **Register** a user via `POST /api/auth/register`
2. Copy the `token` from the response
3. Set the `Authorization` header as `Bearer <token>` for subsequent requests
4. **Create videos** via `POST /api/videos`
5. **Add to watchlist**, **record history**, **view stats**

> 💡 The JWT token is automatically saved to a `{{jwt_token}}` Postman variable on registration/login.

---

## 📂 Project Structure

```
streampulse-backend-microservices/
│
├── docker-compose.yml                 # Orchestrates all services
├── Product_Requirements_Document.md   # Full PRD
├── Tech_Stack.md                      # Technology documentation
├── StreamPulse_API.postman_collection.json  # Postman collection
│
├── config-service/                    # Spring Cloud Config Server
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/
│       ├── java/.../ConfigServiceApplication.java
│       └── resources/
│           ├── application.yml
│           └── config-repo/           # Service configuration files
│
├── discovery-service/                 # Eureka Server
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/
│       ├── java/.../DiscoveryServiceApplication.java
│       └── resources/application.yml
│
├── gateway-service/                   # API Gateway
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/.../
│       ├── GatewayServiceApplication.java
│       ├── config/
│       │   ├── CorsConfig.java        # CORS configuration
│       │   └── ...
│       └── security/
│           ├── AuthenticationFilter.java  # JWT global filter
│           ├── JwtUtil.java               # JWT token utility
│           └── RouteValidator.java        # Open/secured endpoint rules
│
├── video-service/                     # Video Management
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/.../
│       ├── VideoServiceApplication.java
│       ├── controller/
│       │   └── VideoController.java   # REST endpoints
│       ├── model/
│       │   ├── Video.java             # JPA entity
│       │   ├── VideoType.java         # Enum: FILM, SERIE
│       │   └── VideoCategory.java     # Enum: ACTION, COMEDIE, ...
│       ├── dto/
│       │   ├── VideoDTO.java
│       │   ├── TmdbMovieDTO.java
│       │   ├── TmdbMovieDetailDTO.java
│       │   └── TmdbSearchResponse.java
│       ├── service/
│       │   ├── VideoService.java
│       │   ├── VideoServiceImpl.java
│       │   ├── TmdbService.java
│       │   └── TmdbServiceImpl.java
│       ├── repository/
│       │   └── VideoRepository.java
│       ├── mapper/
│       │   └── VideoMapper.java       # MapStruct mapper
│       ├── config/
│       │   ├── TmdbConfig.java        # TMDb WebClient bean
│       │   └── OpenApiConfig.java     # Swagger config
│       └── exception/
│           ├── VideoNotFoundException.java
│           ├── ErrorResponse.java
│           └── GlobalExceptionHandler.java
│
└── user-service/                      # User Management
    ├── Dockerfile
    ├── pom.xml
    └── src/main/java/.../
        ├── UserServiceApplication.java
        ├── controller/
        │   ├── AuthController.java    # Register & Login
        │   └── UserController.java    # User CRUD, watchlist, history
        ├── model/
        │   ├── User.java
        │   ├── Watchlist.java
        │   └── WatchHistory.java
        ├── dto/
        │   ├── UserDTO.java
        │   ├── UserCreateDTO.java
        │   ├── UserUpdateDTO.java
        │   ├── LoginDTO.java
        │   ├── RegisterDTO.java
        │   ├── AuthResponseDTO.java
        │   ├── WatchlistDTO.java
        │   ├── WatchHistoryDTO.java
        │   ├── WatchHistoryCreateDTO.java
        │   └── ViewingStatsDTO.java
        ├── service/
        │   ├── UserService.java
        │   ├── UserServiceImpl.java
        │   ├── AuthService.java
        │   └── AuthServiceImpl.java
        ├── repository/
        │   ├── UserRepository.java
        │   ├── WatchlistRepository.java
        │   └── WatchHistoryRepository.java
        ├── mapper/
        │   └── UserMapper.java        # MapStruct mapper
        ├── client/
        │   ├── VideoServiceClient.java           # OpenFeign client
        │   ├── VideoServiceClientFallbackFactory.java # Resilience fallback
        │   └── VideoDTO.java                      # Client DTO
        ├── security/
        │   ├── SecurityConfig.java    # Spring Security config
        │   ├── JwtTokenProvider.java  # JWT generation & validation
        │   ├── JwtAuthenticationFilter.java  # Request filter
        │   └── CustomUserDetailsService.java # UserDetails loader
        ├── config/
        │   └── OpenApiConfig.java     # Swagger config
        └── exception/
            ├── UserNotFoundException.java
            ├── VideoNotFoundException.java
            ├── ErrorResponse.java
            └── GlobalExceptionHandler.java
```

---

## 🧩 Design Patterns

| Pattern                  | Implementation                                                  |
|--------------------------|-----------------------------------------------------------------|
| **Layered Architecture** | Controller → Service → Repository per service                   |
| **DTO Pattern**          | Separate request/response DTOs decoupled from JPA entities      |
| **Mapper Pattern**       | MapStruct compile-time entity ↔ DTO conversion                  |
| **Repository Pattern**   | Spring Data JPA `JpaRepository` per entity                      |
| **API Gateway**          | Single entry point with Spring Cloud Gateway                    |
| **Service Discovery**    | Eureka-based dynamic registration and lookup                    |
| **Circuit Breaker**      | Resilience4j fallback for inter-service communication           |
| **Database per Service** | Isolated PostgreSQL database per business microservice           |
| **Externalized Config**  | Spring Cloud Config Server for centralized configuration        |
| **Global Exception Handling** | `@ControllerAdvice` with standardized error responses      |

---

## 🔗 Service Ports Summary

| Service           | Port  | Description                    |
|-------------------|-------|--------------------------------|
| Config Service    | 8888  | Centralized configuration      |
| Discovery Service | 8761  | Eureka dashboard & registry    |
| Gateway Service   | 8090  | API Gateway (external access)  |
| Video Service     | 8081  | Video management API           |
| User Service      | 8082  | User, auth, watchlist, history |
| Video DB          | 5432  | PostgreSQL for video-service   |
| User DB           | 5433  | PostgreSQL for user-service    |

---

## 📖 API Documentation (Swagger)

Each business service exposes Swagger UI:

| Service       | Swagger URL                                    |
|---------------|------------------------------------------------|
| Video Service | `http://localhost:8081/swagger-ui/index.html`  |
| User Service  | `http://localhost:8082/swagger-ui/index.html`  |

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/my-feature`
3. Commit your changes: `git commit -m 'Add my feature'`
4. Push to the branch: `git push origin feature/my-feature`
5. Open a Pull Request

---

## 📄 License

This project is licensed under the **MIT License**.

---

<p align="center">
  Made with ☕ and Spring Boot
</p>
