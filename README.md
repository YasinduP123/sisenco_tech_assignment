# Weekly Report Generator & Team Dashboard

A full-stack internal tool for teams to submit structured weekly work reports, run them through a
manager review/correction cycle, and give managers a consolidated dashboard across the whole team.

**Stack:** Next.js (App Router) · Spring Boot · MySQL · Keycloak (authentication)

---

## Architecture

```
Browser → Next.js Frontend (:3000) → Spring Boot Backend (:8082) → MySQL
              ↓                              ↓
         Keycloak (:8080) ←──────────────────┘
```

- **Keycloak** handles identity: registration, login, password storage, and role assignment.
  Neither the frontend nor backend ever touches a raw password.
- **Next.js** renders the UI and forwards the Keycloak-issued JWT on every API call.
- **Spring Boot** validates that JWT, enforces role-based access control, and owns all business logic.
- **MySQL** stores users, projects, reports, task entries, report versions, and review comments.

---

## 1. Prerequisites

| Tool | Version |
|---|---|
| Node.js | 20.x LTS |
| Java | 17+ |
| Maven | 3.9+ |
| MySQL | 8.x |
| Docker (recommended, for Keycloak) | latest |

---

## 2. Running Keycloak

```bash
docker run -p 8080:8080 -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin quay.io/keycloak/keycloak:latest start-dev
```

Then in the admin console (`http://localhost:8080`, login `admin`/`admin`):

1. Create a realm (e.g. `Yasidu`).
2. Create realm roles: `TEAM_MEMBER`, `MANAGER`.
3. Enable self-registration: **Realm Settings → Login → User registration → ON**.
4. Create a **backend** client:
   - Client ID: e.g. `yasidu-client-id`
   - Client authentication: ON
   - Direct access grants: ON (only needed for manual token testing via Postman)
5. Create a **frontend** client (separate from the backend client):
   - Client ID: `weekly-report-frontend`
   - Client authentication: ON
   - Standard flow: ON
   - Valid redirect URIs: `http://localhost:3000/api/auth/callback/keycloak`
   - Valid post logout redirect URIs: `http://localhost:3000/login`
   - Web origins: `http://localhost:3000`
6. Copy the frontend client's secret from its **Credentials** tab — you'll need it below.

---

## 3. Running the Database

```sql
CREATE DATABASE wrm_sisenco_db;
```

Tables are auto-created by Hibernate (`ddl-auto: update`) on first backend startup — no manual
schema script is required.

---

## 4. Running the Backend

```bash
cd backend
```

Edit `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/wrm_sisenco_db
    username: root
    password: <your-mysql-password>
  jpa:
    hibernate:
      ddl-auto: update
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8080/realms/Yasidu

server:
  port: 8082
```

Then:

```bash
mvn clean install
mvn spring-boot:run
```

Backend runs at `http://localhost:8082`. Verify with `GET http://localhost:8082/api/health`.

---

## 5. Running the Frontend

```bash
cd frontend
npm install
```

Copy `.env.local.example` to `.env.local` and fill in real values:

```env
NEXTAUTH_URL=http://localhost:3000
NEXTAUTH_SECRET=<generate with: openssl rand -base64 32>

KEYCLOAK_CLIENT_ID=weekly-report-frontend
KEYCLOAK_CLIENT_SECRET=<from Keycloak client's Credentials tab>
KEYCLOAK_ISSUER=http://localhost:8080/realms/Yasidu

NEXT_PUBLIC_API_BASE_URL=http://localhost:8082/api
NEXT_PUBLIC_KEYCLOAK_ISSUER=http://localhost:8080/realms/Yasidu
NEXT_PUBLIC_KEYCLOAK_CLIENT_ID=weekly-report-frontend
```

```bash
npm run dev
```

Frontend runs at `http://localhost:3000`.

---

## 6. First-Time Use

1. Go to `http://localhost:3000` → redirected to `/login`.
2. Click **Sign in** → Keycloak login screen → click **Register** to create an account.
3. New accounts default to the `TEAM_MEMBER` role. To make someone a manager, sign in as an
   existing manager and change their role from the **Users** page — or assign the `MANAGER`
   realm role directly in Keycloak for the very first account.

---

## Features Implemented

- **Auth:** Registration, login/logout, password protection, and role assignment — all delegated
  to Keycloak. Sessions are stateless JWTs validated on every backend request.
- **Personal report page:** fixed-structure weekly report with a task table (name, type, priority,
  status, planned/actual %, time planned/spent), blockers, achievements, next-week plan, notes.
- **Review/correction workflow:** Draft → Submitted → Needs Correction → Approved, with full
  version history (past content snapshotted on every correction cycle) and a complete comment
  history (not just the latest comment).
- **Team dashboard:** filterable by member/project/status, plus summary metrics and charts
  (submission compliance, tasks-completed trend, status by member, time by task type, recent
  activity feed).
- **Projects/categories:** full CRUD.
- **User management:** role assignment, removal.
- **Role-based access control:** enforced at the URL level (Spring Security), the method level
  (`@PreAuthorize`), and the data level (ownership checks in the service layer).

## Pages

| Route | Role | Purpose |
|---|---|---|
| `/login` | All | Keycloak sign-in / registration |
| `/my-reports` | Team member | Own report history |
| `/my-reports` | Manager | Team-wide filterable report list |
| `/my-reports/new`, `/my-reports/[id]` | Team member | Create/edit a report, view version history |
| `/review/[id]` | Manager | Approve or request changes, with comment history |
| `/dashboard` | Manager | Summary metrics and charts |
| `/projects` | Manager | Project/category CRUD |
| `/users` | Manager | User list, role assignment |

## Known Limitations / Future Improvements

- No AI chat assistant implemented (optional, good-to-have requirement).
- Team-member-to-project assignment not implemented (optional requirement).
- Cross-team side-by-side section comparison view not implemented (bonus).
- No automated frontend tests; one backend RBAC test covers cross-user access denial.
