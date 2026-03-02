# Product Requirements Document (PRD)
## Video Streaming Platform — Microservices Backend

**Version:** 1.0  
**Date:** March 2026  
**Status:** Draft  
**Project Type:** Backend Microservices Application

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Problem Statement](#2-problem-statement)
3. [Objectives](#3-objectives)
4. [Target Users](#4-target-users)
5. [Key Features](#5-key-features)
6. [Functional Requirements](#6-functional-requirements)
7. [Non-Functional Requirements](#7-non-functional-requirements)
8. [User Stories](#8-user-stories)
9. [System Architecture Overview](#9-system-architecture-overview)
10. [API Requirements](#10-api-requirements)
11. [Security Requirements](#11-security-requirements)
12. [Performance Requirements](#12-performance-requirements)
13. [Future Improvements](#13-future-improvements)

---

## 1. Project Overview

The **Video Streaming Platform** is a fully backend-based application built on a **microservices architecture** using Spring Boot and Spring Cloud. It exposes a suite of RESTful APIs that manage video content, users, watchlists, and viewing history. The system is designed for scalability, maintainability, and resilience, following industry-standard architectural patterns including Repository, DTO, and Mapper.

The application has no frontend — all interactions are performed through REST API calls, testable via tools such as Postman. Video trailers are referenced via YouTube embed URLs; no binary video upload is involved.

---

## 2. Problem Statement

Existing monolithic streaming platforms struggle with:

- **Scalability bottlenecks**: A single deployable unit cannot scale individual components independently.
- **Tight coupling**: Business domains (users, content, history) are entangled, making changes risky.
- **Configuration drift**: Managing configuration across environments lacks centralization and versioning.
- **Service discoverability**: Static service references become unmanageable as the system grows.
- **Operational complexity**: No unified entry point for API routing and cross-cutting concerns.

This project addresses these pain points by adopting a cloud-native microservices model with dedicated services, centralized configuration, service discovery, and a unified API gateway.

---

## 3. Objectives

| # | Objective |
|---|-----------|
| 1 | Decompose the streaming platform into independently deployable microservices |
| 2 | Implement a full CRUD API for video content management |
| 3 | Implement user management, watchlist, and viewing history APIs |
| 4 | Integrate external metadata APIs (TMDb or OMDb) for video content enrichment |
| 5 | Enable reliable inter-service communication using OpenFeign |
| 6 | Provide centralized configuration management via a Git-backed Config Service |
| 7 | Ensure all services are discoverable via a Eureka Discovery Service |
| 8 | Route all external traffic through a single API Gateway |
| 9 | Containerize all services with Docker and orchestrate via Docker Compose |
| 10 | Maintain high code quality through unit tests, design patterns, and documentation |

---

## 4. Target Users

Since this is a **100% backend project**, the end consumers are:

| User Type | Description |
|-----------|-------------|
| **Frontend Developers** | Developers consuming REST APIs to build client-side applications |
| **QA Engineers** | Testers validating API behavior via Postman collections |
| **DevOps Engineers** | Engineers deploying, scaling, and monitoring the containerized services |
| **Platform Administrators** | Operators managing video content and user data via API |
| **Backend Developers** | Engineers extending, maintaining, or integrating with the microservices |

---

## 5. Key Features

### 5.1 Video Management
- Full CRUD operations on video content
- External metadata enrichment via TMDb / OMDb public APIs
- Support for video types: `FILM` and `SERIE`
- Support for categories: `ACTION`, `COMEDIE`, `DRAME`, `SCIENCE_FICTION`, `THRILLER`, `HORREUR`
- YouTube embed URL storage for trailers

### 5.2 User Management
- Full CRUD for user accounts
- Secure storage of user credentials

### 5.3 Watchlist
- Add and remove videos from a user's watchlist
- Retrieve the current watchlist for a given user

### 5.4 Viewing History
- Record and track video viewing sessions
- Store progress time and completion status
- Retrieve viewing statistics per user

### 5.5 Infrastructure Services
- **API Gateway**: Single entry point, request routing
- **Service Discovery**: Eureka-based dynamic service registration
- **Centralized Config**: Git-backed externalized configuration (YAML/Properties)
- **Inter-Service Communication**: Declarative REST via OpenFeign

---

## 6. Functional Requirements

### 6.1 Video Service (`video-service`)

| ID | Requirement |
|----|-------------|
| VS-01 | The system shall allow creating a new video entry with all required fields |
| VS-02 | The system shall allow retrieving a video by its unique ID |
| VS-03 | The system shall allow retrieving a list of all videos, with optional filters (type, category) |
| VS-04 | The system shall allow updating any field of an existing video entry |
| VS-05 | The system shall allow deleting a video entry by ID |
| VS-06 | The system shall integrate with TMDb or OMDb API to auto-populate video metadata |
| VS-07 | The system shall store YouTube embed URLs for trailers (no binary video upload) |
| VS-08 | Each video shall include: `id`, `title`, `description`, `thumbnailUrl`, `trailerUrl`, `duration`, `releaseYear`, `type`, `category`, `rating`, `director`, `cast` |
| VS-09 | The video service shall maintain its own isolated relational database |

### 6.2 User Service (`user-service`)

| ID | Requirement |
|----|-------------|
| US-01 | The system shall allow creating a new user with `username`, `email`, and `password` |
| US-02 | The system shall allow retrieving a user profile by ID |
| US-03 | The system shall allow updating user profile information |
| US-04 | The system shall allow deleting a user account |
| US-05 | The system shall allow adding a video to a user's watchlist |
| US-06 | The system shall allow removing a video from a user's watchlist |
| US-07 | The system shall allow retrieving all watchlist entries for a user |
| US-08 | The system shall record a video viewing event with `watchedAt`, `progressTime`, and `completed` fields |
| US-09 | The system shall allow retrieving the full viewing history of a user |
| US-10 | The system shall provide viewing statistics per user (e.g., total videos watched, completion rate) |
| US-11 | The user service shall communicate with video-service via OpenFeign for video data validation/retrieval |
| US-12 | The user service shall maintain its own isolated relational database |

### 6.3 Gateway Service (`gateway-service`)

| ID | Requirement |
|----|-------------|
| GW-01 | The gateway shall act as the single entry point for all external API requests |
| GW-02 | The gateway shall route requests to the appropriate downstream microservice |
| GW-03 | The gateway shall integrate with the Eureka Discovery Service for dynamic routing |

### 6.4 Discovery Service (`discovery-service`)

| ID | Requirement |
|----|-------------|
| DS-01 | The Eureka server shall allow all microservices to register on startup |
| DS-02 | The Eureka server shall provide a service registry dashboard |
| DS-03 | Services shall be able to discover each other by logical name, not hard-coded URLs |

### 6.5 Config Service (`config-service`)

| ID | Requirement |
|----|-------------|
| CS-01 | The config service shall connect to a Git repository (local or remote) |
| CS-02 | Configuration files shall be stored in YAML or Properties format |
| CS-03 | All microservices shall retrieve their configuration from the config service on startup |
| CS-04 | Configuration changes in Git shall be synchronizable without redeployment |

---

## 7. Non-Functional Requirements

### 7.1 Maintainability
- Code must follow a **layered architecture**: Controller → Service → Repository
- All inter-layer data transfer must use **DTOs** (Data Transfer Objects)
- **Mapper** classes/libraries must be used for entity-to-DTO conversion
- Code must be documented with meaningful comments and Javadoc where applicable

### 7.2 Reliability
- Inter-service communication must include appropriate **error handling** and fallback mechanisms
- Service failures should not cascade uncontrollably across services

### 7.3 Scalability
- Each microservice must be independently deployable and scalable
- Services must be stateless to support horizontal scaling

### 7.4 Portability
- All services must be containerized with **Docker**
- Full stack must be orchestrable via **Docker Compose**

### 7.5 Testability
- Unit tests must cover the maximum possible percentage of the codebase
- All APIs must be testable via **Postman**
- A Postman collection should be provided or documented for QA use

### 7.6 Configuration Management
- No hardcoded environment-specific configuration in service code
- All config externalized to the **Config Service** backed by Git

### 7.7 Data Isolation
- Each microservice must own its own database schema
- No direct cross-service database access is permitted

---

## 8. User Stories

### Epic 1: Video Content Management

| ID | User Story | Priority |
|----|-----------|----------|
| U-01 | As an admin, I want to add a new video with metadata so that it appears in the catalog | High |
| U-02 | As an admin, I want to update video details so that information stays accurate | High |
| U-03 | As an admin, I want to delete a video so that outdated content is removed | High |
| U-04 | As a consumer, I want to retrieve a list of all videos so that I can browse the catalog | High |
| U-05 | As a consumer, I want to retrieve a video by ID so that I can get its details | High |
| U-06 | As an admin, I want to fetch video metadata from TMDb/OMDb so I don't have to enter it manually | Medium |

### Epic 2: User Management

| ID | User Story | Priority |
|----|-----------|----------|
| U-07 | As a new user, I want to create an account so I can access the platform | High |
| U-08 | As a user, I want to view my profile so I can see my account information | High |
| U-09 | As a user, I want to update my profile so I can keep my information current | Medium |
| U-10 | As an admin, I want to delete a user account so I can manage the user base | Medium |

### Epic 3: Watchlist

| ID | User Story | Priority |
|----|-----------|----------|
| U-11 | As a user, I want to add a video to my watchlist so I can watch it later | High |
| U-12 | As a user, I want to remove a video from my watchlist so I can keep it organized | High |
| U-13 | As a user, I want to view my full watchlist so I can plan what to watch | High |

### Epic 4: Viewing History & Statistics

| ID | User Story | Priority |
|----|-----------|----------|
| U-14 | As a user, I want my viewing progress to be recorded so I can resume later | High |
| U-15 | As a user, I want to see my viewing history so I can track what I've watched | High |
| U-16 | As a user, I want to see my viewing statistics so I can understand my habits | Medium |

---

## 9. System Architecture Overview

### 9.1 Architecture Diagram (Logical)

```
                        ┌─────────────────────┐
                        │   Config Service     │◄──── Git Repository
                        │  (Spring Cloud       │      (YAML/Properties)
                        │   Config Server)     │
                        └──────────┬──────────┘
                                   │ Config on startup
              ┌────────────────────┼────────────────────┐
              │                    │                    │
   ┌──────────▼──────────┐         │         ┌──────────▼──────────┐
   │   Discovery Service  │◄────────┼─────────│   Gateway Service   │◄── External Clients
   │  (Eureka Server)     │         │         │ (Spring Cloud GW)   │    (Postman / Apps)
   └──────────────────────┘         │         └──────────┬──────────┘
              ▲                     │                    │ Route
              │ Register            │          ┌─────────┴──────────┐
   ┌──────────┴──────────┐          │   ┌──────▼──────┐   ┌────────▼─────┐
   │   Video Service     │◄─────────┤   │User Service │   │Video Service │
   │  (Spring Boot)      │  Feign   │   │(Spring Boot)│   │(Spring Boot) │
   │  ┌──────────────┐   │  Call    │   └──────┬──────┘   └──────┬───────┘
   │  │  Video DB    │   │◄─────────┘          │                 │
   │  └──────────────┘   │               ┌─────▼──────┐   ┌──────▼──────┐
   └─────────────────────┘               │  User DB   │   │  Video DB   │
                                         └────────────┘   └─────────────┘
```

### 9.2 Microservices Summary

| Service | Port (Default) | Role | Database |
|---------|---------------|------|----------|
| `config-service` | 8888 | Centralized configuration from Git | None |
| `discovery-service` | 8761 | Eureka service registry | None |
| `gateway-service` | 8080 | API routing and entry point | None |
| `video-service` | 8081 | Video content CRUD + TMDb/OMDb integration | Relational DB |
| `user-service` | 8082 | User, watchlist, history management | Relational DB |

### 9.3 Key Design Patterns

- **Repository Pattern**: Abstracts data access logic from business logic
- **DTO Pattern**: Separates API data contracts from internal domain models
- **Mapper Pattern**: Handles conversion between entities and DTOs (e.g., MapStruct or ModelMapper)
- **Service Layer Pattern**: Encapsulates business rules between controllers and repositories

---

## 10. API Requirements

### 10.1 Video Service Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/videos` | Create a new video |
| `GET` | `/api/videos` | Get all videos (with optional filters) |
| `GET` | `/api/videos/{id}` | Get a video by ID |
| `PUT` | `/api/videos/{id}` | Update a video by ID |
| `DELETE` | `/api/videos/{id}` | Delete a video by ID |
| `GET` | `/api/videos/search?title=` | Search video by title |
| `GET` | `/api/videos/type/{type}` | Filter by type (FILM/SERIE) |
| `GET` | `/api/videos/category/{category}` | Filter by category |

### 10.2 User Service Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/users` | Create a new user |
| `GET` | `/api/users/{id}` | Get user by ID |
| `PUT` | `/api/users/{id}` | Update user by ID |
| `DELETE` | `/api/users/{id}` | Delete user by ID |
| `POST` | `/api/users/{id}/watchlist/{videoId}` | Add video to watchlist |
| `DELETE` | `/api/users/{id}/watchlist/{videoId}` | Remove video from watchlist |
| `GET` | `/api/users/{id}/watchlist` | Get user's watchlist |
| `POST` | `/api/users/{id}/history` | Record a viewing event |
| `GET` | `/api/users/{id}/history` | Get user's viewing history |
| `GET` | `/api/users/{id}/stats` | Get user's viewing statistics |

### 10.3 API Design Standards

- All endpoints shall return standard HTTP status codes (`200`, `201`, `400`, `404`, `500`)
- Request and response bodies shall use **JSON** format
- Error responses shall include a structured error message with `status`, `error`, and `message` fields
- Endpoints shall be prefixed with `/api/` for clarity

---

## 11. Security Requirements

### 11.1 Baseline (Core Scope)
- Passwords must be stored in hashed form (e.g., BCrypt)
- No sensitive data (passwords, tokens) shall be exposed in API responses
- Input validation must be applied on all incoming request payloads

### 11.2 Bonus — Auth Microservice
- A dedicated **security microservice** should implement **stateless authentication**
- Authentication shall use **JWT (JSON Web Tokens)**
- Protected endpoints shall require a valid Bearer token in the `Authorization` header
- The gateway should validate tokens before forwarding requests downstream
- Token expiration and refresh strategies should be defined

---

## 12. Performance Requirements

| Requirement | Target |
|------------|--------|
| API response time (read operations) | < 300ms under normal load |
| API response time (write operations) | < 500ms under normal load |
| Service startup time (each microservice) | < 30 seconds |
| External API calls (TMDb/OMDb) | Should be cached where appropriate to reduce latency |
| Inter-service Feign calls | Must include timeout and retry configuration |
| Docker Compose startup | Full stack up within 2 minutes |

---

## 13. Future Improvements

| # | Improvement | Description |
|---|------------|-------------|
| 1 | **Auth Microservice** *(Bonus)* | Stateless JWT-based authentication as a dedicated service |
| 2 | **Monitoring Microservice** *(Bonus)* | Centralized metrics collection with Spring Boot Actuator, Prometheus, and Grafana |
| 3 | **Circuit Breaker** | Implement Resilience4j to handle failures in inter-service communication gracefully |
| 4 | **API Rate Limiting** | Add rate limiting at the gateway level to protect downstream services |
| 5 | **Caching Layer** | Introduce Redis for caching frequently accessed video metadata |
| 6 | **Event-Driven Communication** | Replace synchronous Feign calls with Kafka or RabbitMQ for async flows |
| 7 | **API Documentation** | Integrate Swagger/OpenAPI for auto-generated, interactive API documentation |
| 8 | **CI/CD Pipeline** | Automate testing and deployment via GitHub Actions or Jenkins |
| 9 | **Search & Filtering** | Implement Elasticsearch for advanced video search capabilities |
| 10 | **Recommendation Engine** | Add a recommendation service based on viewing history and preferences |

---

*Document prepared for internal development use. All specifications are subject to revision during sprint planning and backlog grooming.*
