# Stockfela – Digital ROSCA / Stokvel Platform

> **Stockfela** is a Spring Boot REST API that digitalises the traditional South African **stokvel** (also known internationally as a ROSCA — *Rotating Savings and Credit Association*). Members pool a fixed monthly contribution; each month the full pot rotates to one member until everyone has received it exactly once.

---

## Table of Contents

1. [What is a Stokvel / ROSCA?](#what-is-a-stokvel--rosca)
2. [Tech Stack](#tech-stack)
3. [Project Structure](#project-structure)
4. [Architecture](#architecture)
5. [Entity Relationship Diagram](#entity-relationship-diagram)
6. [API Reference](#api-reference)
7. [Getting Started](#getting-started)
8. [Environment Variables](#environment-variables)
9. [Security Model](#security-model)
10. [Running Tests](#running-tests)

---

## What is a Stokvel / ROSCA?

A **stokvel** works like this:

| Month | Members contribute | Recipient |
|-------|--------------------|-----------|
| 1     | A, B, C, D each pay R1 000 | Member A receives R4 000 |
| 2     | A, B, C, D each pay R1 000 | Member B receives R4 000 |
| 3     | A, B, C, D each pay R1 000 | Member C receives R4 000 |
| 4     | A, B, C, D each pay R1 000 | Member D receives R4 000 |

At the end, every member has contributed R4 000 total and received R4 000 total — but the lump-sum payout lets them make a big purchase or investment they couldn't otherwise afford.

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 21 |
| Framework | Spring Boot 3.5.6 |
| Security | Spring Security + JWT (jjwt 0.11.5) |
| Persistence | Spring Data JPA + Hibernate |
| Database (dev) | H2 in-memory |
| Database (prod) | MySQL 8 |
| Validation | Jakarta Bean Validation |
| Boilerplate | Lombok |
| Build | Maven |
| API Collection | Bruno (`StockfelaCollection/`) |

---

## Project Structure

```
Stockfela/
├── .env.example                   # Copy to .env and fill in secrets
├── .gitignore
├── README.md
├── StockfelaCollection/           # Bruno API request collection
│   ├── LoginUSer.bru
│   └── RegisterUser.bru
└── stockfela/stockfela/
    └── src/
        ├── main/
        │   ├── java/com/application/stockfela/
        │   │   ├── StockfelaApplication.java   # Entry point
        │   │   ├── JWT/
        │   │   │   ├── AuthEntryPointJWT.java  # 401 JSON handler
        │   │   │   ├── AuthTokenFilter.java    # JWT filter (runs every request)
        │   │   │   └── JWTUtilities.java       # Token generation & validation
        │   │   ├── config/
        │   │   │   ├── AppConfig.java          # General config placeholder
        │   │   │   ├── RoleSeeder.java         # Seeds ROLE_USER / ROLE_ADMIN
        │   │   │   └── SecurityConfig.java     # Routes, CORS, session policy
        │   │   ├── controller/
        │   │   │   ├── AuthController.java     # POST /api/auth/register, /login
        │   │   │   ├── GroupController.java    # CRUD for savings groups
        │   │   │   └── PayoutController.java   # Record payments, query progress
        │   │   ├── dto/
        │   │   │   ├── ProcessStatusCodes.java # Status code enum
        │   │   │   ├── StockfelaMapper.java    # Entity to DTO mapping
        │   │   │   ├── request/               # Request body DTOs
        │   │   │   └── response/              # Response body DTOs
        │   │   ├── entity/                    # JPA entities
        │   │   ├── globalException/           # @RestControllerAdvice handler
        │   │   ├── repository/               # Spring Data JPA repositories
        │   │   └── service/                  # Business logic
        │   └── resources/
        │       └── application.yml            # All config (secrets from env vars)
        └── test/
            └── java/.../StockfelaApplicationTests.java
```

---

## Architecture

```
HTTP Client (Browser / Mobile / Bruno / Postman)
          |
          | HTTPS  (Authorization: Bearer <JWT>)
          v
+----------------------------------------------------------+
|                  Spring Security Layer                   |
|  AuthTokenFilter (OncePerRequestFilter)                  |
|    1. Extract JWT from Authorization header              |
|    2. Validate signature + expiry (JWTUtilities)         |
|    3. Load UserDetails, set SecurityContext              |
|  SecurityFilterChain (SecurityConfig)                    |
|    /api/auth/**     -> public                            |
|    /h2-console/**   -> public (dev only)                 |
|    everything else  -> requires valid JWT                |
+----------------------------------------------------------+
          |
          v
+----------------------------------------------------------+
|                    Controller Layer                      |
|  AuthController   /api/auth/register  /api/auth/login   |
|  GroupController  /api/groups/**                        |
|  PayoutController /api/payouts/**                       |
+----------------------------------------------------------+
          |
          v
+----------------------------------------------------------+
|                     Service Layer                        |
|  UserService         - register, loadUserByUsername      |
|  SavingGroupService  - createGroup, addMember, cycles    |
|  PayoutCycleService  - contributions, recordPayment      |
+----------------------------------------------------------+
          |
          v
+----------------------------------------------------------+
|                   Repository Layer                       |
|  UserRepo  RoleRepo  SavingsGroupRepo                    |
|  GroupMemberRepo  ContributionRepo  PayoutCycleRepo      |
+----------------------------------------------------------+
          |  JPA / Hibernate
          v
+----------------------------------------------------------+
|                      Database                            |
|   H2 in-memory (dev)   |   MySQL 8 (production)         |
+----------------------------------------------------------+
```

---

## Entity Relationship Diagram

```
users ----< group_members >---- savings_groups
  |                                   |
  |                                   |
  +-------> payout_cycles <-----------+
                 |
                 |
           contributions
```

**Detailed fields:**

```
users                      savings_groups
-----                      --------------
id PK                      id PK
username (unique)          name
email (unique)             description
password (BCrypt)          monthly_contribution
full_name                  cycle_months
phone_number               current_cycle
enabled                    status (ACTIVE/COMPLETED/CANCELLED)
created_at                 created_by FK -> users.id
updated_at                 created_at

group_members              payout_cycles
-------------              -------------
id PK                      id PK
group_id FK                group_id FK -> savings_groups.id
user_id FK                 recipient_user_id FK -> users.id
payout_order               cycle_number
has_received_payout        total_amount
joined_at                  payout_date
                           status (PENDING/COMPLETED/FAILED)
                           created_at

contributions              roles              user_roles (join)
-------------              -----              ----------------
id PK                      id PK              user_id FK
payout_cycle_id FK         name (unique)      role_id FK
user_id FK                   ROLE_USER
group_id FK                  ROLE_ADMIN
amount
status (PENDING/PAID/OVERDUE)
payment_date
paid_at
created_at
```

---

## API Reference

All endpoints except `/api/auth/*` require `Authorization: Bearer <JWT>`.

### Authentication

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/api/auth/register` | No | Register a new user |
| `POST` | `/api/auth/login` | No | Log in and receive a JWT |

**Register request body:**
```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "securepass123",
  "fullName": "John Doe",
  "phoneNumber": "+27821234567",
  "roles": ["ROLE_USER"]
}
```

**Login request body:**
```json
{ "username": "john_doe", "password": "securepass123" }
```

**Login response:**
```json
{
  "jwtToken": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "john_doe",
  "roles": ["ROLE_USER"]
}
```

### Savings Groups

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/api/groups` | JWT | Create a new group |
| `GET` | `/api/groups/my` | JWT | Get current user's groups |
| `GET` | `/api/groups/{id}` | JWT | Get group details |
| `GET` | `/api/groups/{id}/members` | JWT | List group members |
| `POST` | `/api/groups/{id}/members` | JWT | Add a member |
| `POST` | `/api/groups/{id}/payout-cycle` | JWT | Start next payout cycle |

**Create group:**
```json
{
  "name": "Family Stokvel",
  "description": "Monthly family savings",
  "monthlyContribution": 1000.00,
  "cycleMonths": 6
}
```

### Payouts & Contributions

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/api/payouts/{cycleId}/pay` | JWT | Record a payment |
| `GET` | `/api/payouts/{cycleId}/progress` | JWT | Get payment progress |
| `GET` | `/api/payouts/{cycleId}/contributions` | JWT | List contributions |

---

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.9+
- (Optional) MySQL 8 for production

### 1. Clone

```bash
git clone https://github.com/NovasOrbit/stockfela.git
cd Stockfela
```

### 2. Configure environment

```bash
cp .env.example .env
# Edit .env — generate JWT_SECRET with: openssl rand -base64 64
```

### 3. Run (H2 dev mode — no database setup required)

```bash
cd stockfela/stockfela
./mvnw spring-boot:run
```

API: `http://localhost:8080`  
H2 Console: `http://localhost:8080/h2-console`

### 4. Switch to MySQL

Set in your `.env`:

```env
DB_URL=jdbc:mysql://localhost:3306/stockfela_db?useSSL=false&serverTimezone=UTC
DB_DRIVER=com.mysql.cj.jdbc.Driver
DB_USERNAME=stockfela_user
DB_PASSWORD=your_secure_password
DB_PLATFORM=org.hibernate.dialect.MySQL8Dialect
```

Create the schema:
```sql
CREATE DATABASE stockfela_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'stockfela_user'@'localhost' IDENTIFIED BY 'your_secure_password';
GRANT ALL PRIVILEGES ON stockfela_db.* TO 'stockfela_user'@'localhost';
```

### 5. Import API collection

Open [Bruno](https://www.usebruno.com/) and import `StockfelaCollection/`.

---

## Environment Variables

See [`.env.example`](.env.example) for the full list.

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `JWT_SECRET` | **Yes** | — | Base64 HMAC-SHA256 key (min 32 bytes). Generate: `openssl rand -base64 64` |
| `JWT_EXPIRATION_MS` | No | `86400000` | Token lifetime in ms (24 h) |
| `DB_URL` | No | H2 in-memory | JDBC connection URL |
| `DB_USERNAME` | No | `sa` | Database username |
| `DB_PASSWORD` | No | *(empty)* | Database password |
| `DB_DRIVER` | No | H2 driver | JDBC driver class name |
| `DB_PLATFORM` | No | H2 dialect | Hibernate dialect |
| `ALLOWED_ORIGINS` | No | `http://localhost:5174` | Comma-separated CORS origins |
| `SERVER_PORT` | No | `8080` | HTTP port |

---

## Security Model

| Concern | Approach |
|---------|----------|
| Authentication | JWT (HS256) — stateless, no server-side sessions |
| Password storage | BCrypt (strength 10) |
| Secret management | All secrets in environment variables; `.env` is gitignored |
| CORS | Explicit allowlist via `ALLOWED_ORIGINS`; never `*` wildcard |
| Route protection | Only `/api/auth/**` is public; everything else requires JWT |
| IDOR prevention | Group creator derived from JWT principal, never from request body |
| Error messages | Stack traces never exposed; generic messages for 5xx errors |
| H2 console | `web-allow-others: false` — localhost only, no remote access |
| Username enumeration | Login errors use a generic message regardless of which field is wrong |

---

## Running Tests

```bash
cd stockfela/stockfela
./mvnw test
```

The default test loads the full Spring context with H2. Unit tests can be added under `src/test/java/com/application/stockfela/`.
