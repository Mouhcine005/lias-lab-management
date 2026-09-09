# LIAS Lab Management System

A full-stack laboratory management system built for the LIAS research lab. The backend is a REST API built with Spring Boot 4, secured with JWT authentication and role-based access control. The frontend is a React + TypeScript SPA.

---

## Tech Stack

**Backend**
- Java 21 / Spring Boot 4.0.6
- Spring Security 7 (stateless JWT)
- Spring Data JPA + PostgreSQL
- iText 8 (PDF report generation)
- JavaMailSender (email notifications)
- Lombok / Maven

**Frontend**
- React + TypeScript + Vite
- TanStack Query (data fetching)
- Axios
- Tailwind CSS
- React Router (role-based routing)

---

## Features

| Module | Description |
|---|---|
| **Authentication** | Register / login with JWT. New accounts start as `PENDING` until approved by a director. |
| **Members** | Full member profiles — name, photo, biography, research interests, affiliation history, hire date. |
| **Publications** | Create and browse lab publications, filterable by year or team. |
| **Events** | Lab events visible to all authenticated users, managed by admins/directors. |
| **Meetings** | Meeting records with file attachment support (minutes, agendas). |
| **Documents** | Shared document storage with upload/download. |
| **Equipment** | Equipment inventory with request/approval workflow. |
| **Reports** | Annual and monthly PDF reports generated on-demand (iText 8), downloadable by admins/directors. |
| **Notifications** | In-app + email notifications (e.g. new member pending approval). |

---

## Roles & Permissions

| Role | Access |
|---|---|
| `ADMIN` | Full access — manage members, equipment, documents, generate reports |
| `DIRECTOR` | View all members, approve/reject pending accounts, generate reports |
| `MEMBER` | View own profile, publications, events, meetings, documents |
| `DOCTORAL` | Same as MEMBER |
| `VISITOR` | Read-only, limited access |

---

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.9+
- PostgreSQL 15+
- Node.js 20+ (for the frontend)

### 1. Clone the repository

```bash
git clone https://github.com/Mouhcine005/lias-lab-management.git
cd lias-lab-management
```

### 2. Create the database

```sql
CREATE DATABASE lias_db;
```

### 3. Configure the backend

Copy the example config and fill in your values:

```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

Then edit `application.properties`:

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/lias_db
spring.datasource.username=YOUR_DB_USERNAME
spring.datasource.password=YOUR_DB_PASSWORD

# JWT — use a random string of at least 32 characters
app.jwt.secret=YOUR_JWT_SECRET_MIN_32_CHARS
app.jwt.expiration=86400000

# Mail (for notifications)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=YOUR_EMAIL
spring.mail.password=YOUR_APP_PASSWORD
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

> **Note:** `application.properties` is gitignored — your credentials will never be committed.

### 4. Run the backend

```bash
./mvnw spring-boot:run
```

The API starts at `http://localhost:8080`.

Interactive API docs are available at:
```
http://localhost:8080/api-docs
```

### 5. Run the frontend

```bash
cd frontend
npm install
npm run dev
```

The frontend starts at `http://localhost:5173`.

---

## API Overview

### Auth (public)

```
POST /api/auth/register    — create account (starts as PENDING)
POST /api/auth/login       — returns JWT token
```

### Members

```
GET  /api/members/all      — list all (ADMIN, DIRECTOR)
GET  /api/members/{id}     — get profile (ADMIN, DIRECTOR, MEMBER, DOCTORAL)
GET  /api/members/me       — get own profile (any authenticated)
PUT  /api/members/me       — update own profile (any authenticated)
```

### Publications

```
GET  /api/publications              — all publications
GET  /api/publications/year/{year}  — filter by year
GET  /api/publications/team/{team}  — filter by team
GET  /api/publications/me           — my publications
POST /api/publications              — create (ADMIN, DIRECTOR)
```

### Equipment

```
GET  /api/equipment                 — view inventory
POST /api/equipment                 — add item (ADMIN)
PUT  /api/equipment/{id}            — update item (ADMIN)
DELETE /api/equipment/{id}          — delete item (ADMIN)
POST /api/equipment/{id}/request    — request equipment (MEMBER)
```

### Reports

```
GET  /api/report/annual?year=2025   — download annual PDF (ADMIN, DIRECTOR)
GET  /api/report/monthly?year=2025&month=3 — download monthly PDF (ADMIN, DIRECTOR)
```

All endpoints except `/api/auth/**` and `/api-docs` require a valid `Authorization: Bearer <token>` header.

---

## Project Structure

```
src/main/java/com/lias/lias_backend/
├── config/          # Security, CORS config
├── security/        # JWT filter, JwtUtil, UserDetailsService
├── member/          # Auth, Member entity, profiles
├── publication/     # Publications module
├── event/           # Events module
├── meeting/         # Meetings + file attachments
├── document/        # Document storage
├── equipment/       # Equipment inventory & requests
├── report/          # PDF report generation (iText 8)
├── notification/    # In-app + email notifications
└── LiasBackendApplication.java
```

---

## Security Notes

- Passwords are hashed with BCrypt
- JWT tokens are signed with HMAC-SHA and validated on every request
- New accounts require director approval before activation
- File uploads are stored on disk under `uploads/` (gitignored)
