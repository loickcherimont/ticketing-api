# 🎧 Ticketing API  

  This is a **back-end REST API** made with **Spring Boot** for efficient helpdesk ticket tracking and resolution management. It features JWT-based authentication, role-based access control (USER/AGENT), and comprehensive API documentation with Swagger UI.

## 🖥️ Tech Stack

**Backend:**
- **Java 21** — Modern language features, better performance
- **Spring Boot 3.5.4** — Web framework, REST controllers, Data JPA
- **Spring Security** — Security, JWT authentication & role-based authorization
- **PostgreSQL 15+** — Production database (required for dev setup)
- **jjwt 0.11.5** — JWT token signing & validation
- **JUnit 5 + Mockito** — Unit testing (all mocked)

**Frontend (Static Assets):**
- **Bootstrap 5.3.3** — Responsive UI framework

**DevOps:**
- **Railway** — Server hosting
- **Supabase** — Database hosting (production)
- **Docker** — Server containerization (production)
- **Docker Compose** — PostgreSQL containerization (development)
- **Git / GitHub** — Version control

## 🔐 Authentication & Authorization

### JWT Token Flow

1. **Sign in** → POST `/api/auth/signin` with email & password
2. **Receive JWT token** → Token expires after configured time
3. **Use token** → Send in `Authorization: Bearer <token>` header
4. **Token validated** → Spring Security authenticates every request

### User Roles

| Role | Permissions |
|------|-------------|
| **USER** | • Create tickets<br/>• View all tickets<br/>• View ticket details |
| **AGENT** | • All USER permissions<br/>• Claim tickets (set IN_PROGRESS)<br/>• Resolve tickets (add solution) |

### Endpoint Access Matrix

| Endpoint | Method | AGENT | USER | Public |
|----------|--------|-------|------|--------|
| `/api/auth/signin` | POST | ✅ | ✅ | ✅ |
| `/api/tickets` | GET | ✅ | ✅ | ❌ |
| `/api/tickets/{id}` | GET | ✅ | ✅ | ❌ |
| `/api/tickets` | POST | ✅ | ✅ | ❌ |
| `/api/tickets/agent/{id}/in-progress` | PATCH | ✅ | ❌ | ❌ |
| `/api/tickets/agent/{id}/solve` | PATCH | ✅ | ❌ | ❌ |

## 🚀 Setup

### Quick Start (PostgreSQL)

```bash
# 1. Clone repository
git clone https://github.com/loickcherimont/ticketing-api.git
cd ticketing-api

# 2. Start PostgreSQL container
docker compose up -d db

# 3. Run the API with Maven
./mvnw spring-boot:run

# API is now running on http://localhost:8080
``` 

**Access the API:**
- **Swagger UI (interactive docs)** → http://localhost:8080/swagger-ui.html
- **API Root** → http://localhost:8080/

### Clean Up

```bash
# Remove PostgreSQL container and unused Docker resources
# ⚠️ WARNING: This deletes all database data. Push changes first!
docker compose down
docker system prune
```


## ▶️ Usage

> [!IMPORTANT]
> To test each route, use the following credentials:
>
> | Role | Username | Password |
> |------|----------|----------|
> | USER | `john.doe@gmail.com` | `test123` |
> | AGENT | `agent@company.com` | `agent123` |

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/auth/signin` | Get JWT token | ✅ Public |
| GET | `/api/tickets` | List all tickets | 🔒 Required |
| GET | `/api/tickets/{id}` | Get ticket by ID | 🔒 Required |
| POST | `/api/tickets` | Create new ticket | 🔒 Required |
| PATCH | `/api/tickets/agent/{id}/in-progress` | Claim ticket | 🔒 AGENT only |
| PATCH | `/api/tickets/agent/{id}/solve` | Resolve ticket | 🔒 AGENT only |

### 1. Sign In (Get JWT Token)

```bash
curl -X POST http://localhost:8080/api/auth/signin \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "email": "user@example.com",
  "role": "USER"
}
```

### 2. Create a Ticket (USER role)

```bash
curl -X POST http://localhost:8080/api/tickets \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..." \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Login page not working",
    "description": "Cannot sign in to account after password reset"
  }'
```

**Response (201 Created):**
```json
{
  "id": 1,
  "title": "Login page not working",
  "description": "Cannot sign in to account after password reset",
  "status": "OPEN",
  "solution": null
}
```

### 3. View All Tickets (USER/AGENT role)

```bash
curl -X GET http://localhost:8080/api/tickets \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..."
```

### 4. Claim Ticket (AGENT role only)

```bash
curl -X PATCH http://localhost:8080/api/tickets/agent/1/in-progress \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..."
```

### 5. Resolve Ticket (AGENT role only)

```bash
curl -X PATCH http://localhost:8080/api/tickets/agent/1/solve \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..." \
  -H "Content-Type: application/json" \
  -d '{
    "solution": "Password reset link sent to registered email. User can now sign in."
  }'
```

## 📄 API Documentation

**Full interactive documentation on Railway** with request/response examples:
→ https://ticketing-api-production-92ac.up.railway.app/swagger-ui/index.html

![Preview API documentation](.github/endpoints-docs.png 'API docs with Swagger UI | Ticket API')

## 🔑 License

<div align="center">Copyright &copy; 2026 | Loick CHERIMONT | All Rights Reserved.</div>
