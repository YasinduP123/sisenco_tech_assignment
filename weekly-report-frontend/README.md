# Weekly Report Frontend

Next.js (App Router) frontend for the Weekly Report Generator & Team Dashboard.
Talks to the Spring Boot backend via REST, authenticates through Keycloak using NextAuth.

## 1. Install dependencies

```bash
npm install
```

## 2. Configure environment

Copy `.env.local.example` to `.env.local` and fill in real values:

```bash
cp .env.local.example .env.local
```

- `NEXTAUTH_SECRET` — generate with `openssl rand -base64 32`
- `KEYCLOAK_CLIENT_ID` / `KEYCLOAK_CLIENT_SECRET` — from your Keycloak client's Credentials tab
- `KEYCLOAK_ISSUER` — `http://localhost:8080/realms/<your-realm-name>`
- `NEXT_PUBLIC_API_BASE_URL` — your Spring Boot backend base URL (e.g. `http://localhost:8082/api`)

## 3. Keycloak client requirements

Create a **separate** Keycloak client for the frontend (do not reuse the backend's client):

- Client ID: `weekly-report-frontend` (or match your `.env.local`)
- Client authentication: ON (confidential)
- Valid redirect URIs: `http://localhost:3000/api/auth/callback/keycloak`
- Web origins: `http://localhost:3000`

## 4. Run the dev server

```bash
npm run dev
```

App runs at `http://localhost:3000`.

## Pages included

| Route | Who | Purpose |
|---|---|---|
| `/login` | Everyone | Keycloak sign-in |
| `/my-reports` | Team member | Own report history; Manager sees team-wide filterable list |
| `/my-reports/new` | Team member | Create a new draft report |
| `/my-reports/[id]` | Team member | Edit/view own report, submit for review |
| `/review/[id]` | Manager | Review a submitted report — approve or request changes |
| `/dashboard` | Manager | Summary metrics, charts, recent activity |
| `/projects` | Manager | Project/category CRUD |
| `/users` | Manager | User list, role assignment |

Role-based route protection is enforced in `middleware.ts` (client-side redirect) —
the real enforcement is server-side in Spring Boot (`@PreAuthorize`), this is just UX.

## Notes

- No task-detail endpoint calls are separate — task rows are submitted as part of the report payload (`taskEntries`).
- Version history is fetched on-demand via `getReportVersionHistory`, shown when a report is in `NEEDS_CORRECTION`.
- Comment history is fetched on the review page via `getCommentHistory`.
