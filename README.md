# Auth Service

Part of the **Employee Travel Desk (ETD)** system — a Cognizant FSE Business Aligned Project.

This microservice is the **centralised authentication service** for the entire ETD platform. It handles login, token refresh, logout and token blacklist checks. Every other ETD service delegates all auth decisions to this service.

---

## What this service does

| Responsibility | Details |
|---|---|
| **Login** | Authenticates credentials and issues a JWT access token (1 hour) + refresh token (7 days) |
| **Token refresh** | Rotates tokens — old refresh token deleted, new pair issued |
| **Logout** | Deletes refresh token + blacklists the access token so it cannot be reused |
| **Blacklist check** | Exposes `GET /auth/blacklist/check` — called by every other service on each incoming request |
| **Token cleanup** | Purges expired blacklist entries on startup |

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5.7 |
| Security | Spring Security + JWT issuance (JJWT 0.12.6) |
| ORM | Spring Data JPA / Hibernate |
| Database | Shared H2 via TCP (account-management hosts the DB) |
| Build tool | Gradle |
| Utilities | Lombok, Springdoc OpenAPI |

---

## Prerequisites

- Java 21
- Gradle (system install — wrapper jar is not committed)
- **account-management must be running first** — it starts the H2 TCP server on port 9092 that this service connects to

---

## Service Startup Order

```
1. account-management (port 8081)  ← start first — starts H2 TCP server on port 9092
2. auth-service (port 8080)        ← start second — connects to H2 TCP server
3. travel-planner (port 8082)      ← start after both above
```

---

## Running the application

```bash
# Build
gradle build

# Run — starts on port 8080
gradle bootRun

# Run tests
gradle test

# Clean build output
gradle clean
```

On startup **DataInitializer** automatically:
- Purges expired entries from the `token_blacklist` table

> Employee and grade seeding is done by **account-management's DataInitializer** — not here.

---

## Configuration

### application.properties

```properties
server.port=8080

# Connects to account-management's H2 TCP server (must be running first)
spring.datasource.url=jdbc:h2:tcp://localhost:9092/~/data/account_management

# auth-service does NOT own the schema — account-management manages it
spring.jpa.hibernate.ddl-auto=none

# Shared secret — must match every other ETD service
jwt.secret=etdTravelDeskJwtSecretKey1234567890ABCDEF
```

> **Important:** `jwt.secret` must be identical across all ETD services. Changing it in one service invalidates all active tokens issued by the others.

---

## Database

auth-service does **not** have its own database. It connects to account-management's H2 database via TCP:

| Setting | Value |
|---|---|
| JDBC URL | `jdbc:h2:tcp://localhost:9092/~/data/account_management` |
| Username | `sa` |
| Password | *(blank)* |
| H2 Console | `http://localhost:8080/h2-console` |

Tables used:

| Table | Owned by | auth-service access |
|---|---|---|
| `employees` | account-management | Read-only (authentication only) |
| `refresh_tokens` | auth-service | Full read/write |
| `token_blacklist` | auth-service | Full read/write |

> `ddl-auto=none` — account-management owns all schema creation. auth-service only reads/writes data.

---

## Default Credentials

Accounts are seeded by **account-management** on first startup. Use these to log in:

| Role | Email | Password |
|---|---|---|
| HR | `admin.hr@cognizant.com` | `Admin@123` |
| TravelDeskExe | `desk.exec@cognizant.com` | `Exec@123` |
| Employee | `john.employee@cognizant.com` | `Employee@123` |

---

## API Reference

Base URL: `http://localhost:8080`

All endpoints are **open** — no `Authorization` header required.

---

### POST /login

Authenticates the user and returns a JWT access token + refresh token.

**Request:**
```json
{
  "emailAddress": "admin.hr@cognizant.com",
  "password": "Admin@123"
}
```

**Response `200`:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "a3f9b2c1-d4e5-6f78-90ab-cdef12345678",
  "emailAddress": "admin.hr@cognizant.com",
  "role": "HR"
}
```

> `role` values: `HR`, `Employee`, `TravelDeskExe`
> Store both tokens. Use `token` in the `Authorization: Bearer` header for all API calls. Store `refreshToken` securely and only send it to `/auth/refresh`.

---

### POST /auth/refresh

Rotates both tokens. The old refresh token is deleted and a new pair is issued.

**Request:**
```json
{
  "refreshToken": "a3f9b2c1-d4e5-6f78-90ab-cdef12345678"
}
```

**Response `200`:** Same structure as `/login` — save both new tokens immediately.

> Call this when any ETD service returns `403` due to an expired access token.

---

### POST /auth/logout

Deletes the refresh token and blacklists the access token.

**Headers:**
```
Authorization: Bearer <accessToken>
Content-Type: application/json
```

**Request:**
```json
{
  "refreshToken": "a3f9b2c1-d4e5-6f78-90ab-cdef12345678"
}
```

**Response `204 No Content`**

> Always send both the `Authorization` header and the `refreshToken` body.
> Without the header — the refresh token is deleted but the access token stays valid for the remainder of its 1-hour window.
> With the header — the access token is immediately blacklisted and rejected by all ETD services.

---

### GET /auth/blacklist/check

Checks whether an access token has been invalidated via logout.

**Query param:** `token=<accessToken>`

```
GET /auth/blacklist/check?token=eyJhbGciOiJIUzI1NiJ9...
```

**Response `200`:**
```json
true   // token is blacklisted — reject this request
false  // token is valid (not blacklisted)
```

> This endpoint is called internally by account-management and travel-planner on every incoming request. It is not typically called by frontend clients directly.

---

## Token Lifecycle

```
┌─────────────────────────────────────────────────────────────────┐
│  POST /login                                                     │
│    → accessToken  (JWT, 1 hour)   store in memory / header      │
│    → refreshToken (UUID, 7 days)  store securely                │
└──────────────────────────┬──────────────────────────────────────┘
                           │
          Use accessToken on every API call
          Authorization: Bearer <accessToken>
                           │
          ┌────────────────▼─────────────────┐
          │ accessToken expires (1 hour)      │
          │  → API returns 403               │
          │  → call POST /auth/refresh       │
          │  → get new accessToken           │
          │  → get new refreshToken          │
          └────────────────┬─────────────────┘
                           │
          ┌────────────────▼─────────────────┐
          │ refreshToken expires (7 days)     │
          │  → POST /auth/refresh returns 400 │
          │  → must POST /login again         │
          └──────────────────────────────────┘

  POST /auth/logout
    → refreshToken deleted (no new tokens possible)
    → accessToken blacklisted
    → all ETD services reject the token immediately
```

---

## JWT Token Structure

```json
{
  "sub": "admin.hr@cognizant.com",
  "role": "HR",
  "iat": 1749135600,
  "exp": 1749139200
}
```

| Claim | Value |
|---|---|
| `sub` | Employee email address |
| `role` | `HR` / `Employee` / `TravelDeskExe` |
| `iat` | Issued at (Unix timestamp) |
| `exp` | Expires at (Unix timestamp — 1 hour after `iat`) |

The `role` claim is extracted by all services from the token directly — no database call needed for role-based access control.

---

## Error Response Format

```json
{
  "message": "Human-readable description",
  "fieldName": "Field that caused the error (nullable)",
  "status": "HTTP status name"
}
```

| HTTP Status | Scenario |
|---|---|
| `400 BAD_REQUEST` | Invalid credentials, invalid/expired refresh token |
| `404 NOT_FOUND` | Employee not found |

---

## Swagger UI

- **UI:** `http://localhost:8080/swagger-ui.html`
- **JSON spec:** `http://localhost:8080/v3/api-docs`

---

## Project Structure

```
src/main/java/com/etd/auth_service/
├── config/           SecurityConfig  (all routes permitAll + auth beans)
│                     DataInitializer  (cleans up expired blacklist entries on startup)
├── constant/         AppConstant  (message keys)
├── controller/       AuthController   (POST /login, POST /auth/refresh, POST /auth/logout)
│                     BlacklistController  (GET /auth/blacklist/check)
├── dao/              EmployeeRepo, RefreshTokenRepo, TokenBlacklistRepo
├── dto/              AuthRequestDTO, AuthResponseDTO, RefreshRequestDTO, ErrorDTO
├── entity/           Employee (read-only), RefreshToken, TokenBlacklist
├── exception/        BadRequestException, GlobalExceptionHandler
├── service/
│   ├── interfaces/   RefreshTokenService, TokenBlacklistService
│   └── classes/      MyUserDetailService, RefreshTokenServiceImpl, TokenBlacklistServiceImpl
└── util/             JWTUtil  (generateToken, extractUsername, extractExpiration)
```

---

## Related Services

| Service | Port | Responsibility |
|---|---|---|
| **auth-service** *(this service)* | **8080** | Login, token refresh, logout, blacklist check |
| account-management | 8081 | Employee / grade / grade-history CRUD + H2 TCP server host |
| travel-planner | 8082 | Travel request lifecycle, budget calculation |
| reservation-management | — | Flight / hotel / cab reservation upload and tracking |
| reimbursement-management | — | Expense claim submission and processing |

All other services call `GET /auth/blacklist/check` via a Feign client (`AuthServiceClient`) on every incoming request to enforce immediate token invalidation after logout.
