## PlanMate

PlanMate is a group expense tracking app: create groups, add members, log shared expenses split among participants, and let the app work out who owes whom.

---
## Features
- Create and manage groups
- Add members to groups
- Track shared expenses with multiple participants
- Calculate and display settlements (who owes whom)
- Real-time updates with WebSockets
- Authentication and Authorization with Keycloak
- Redis caching for optimized settlement calculation

---
## Tech stack
**Backend**
- Java 25, Spring Boot
- Spring Data JPA (Postgres)
- Spring Cache (Redis)
- Spring WebSockets
- Spring Security (OAuth2 resource server) + Keycloak
- Flyway (DB migrations)
- MapStruct (entity ↔ DTO mapping), Lombok
- springdoc-openapi (Swagger UI)
- JUnit 5, Mockito, Jacoco (tests & coverage)

**Frontend**
- React + Vite (Javascript)
- Tailwind CSS (UI styling)
- Redux Toolkit (state management)
- React Router
- Axios (API requests)
- STOMP/SockJS (WebSocket client)
- react-oauth2-code-pkce (Keycloak PKCE login)
- Framer Motion (animations), react-hot-toast (notifications)

**Infrastructure**
- Docker + Docker Compose (local dev)
- GitHub Actions (CI/CD), GitHub Container Registry (image hosting)
- Render (Keycloak + backend as Docker web services, frontend as a static site), Neon (Postgres), Upstash (Redis)

---
## Getting Started

### Prerequisites
Docker + Docker Compose — that's the only thing you need installed.

### Setup
1. Copy `backend/backend.env.example` to `backend/backend.env`, and `frontend/.env.example` to `frontend/.env`. The defaults in these files are pre-wired to work with each other and with the committed Keycloak realm, so no edits are needed for a local run.
2. From `docker/planmate`, build and start everything with Docker Compose. This brings up Postgres, Redis, Keycloak, the backend, and the frontend, and imports the Keycloak realm automatically on first start.
3. Once everything is healthy: the frontend is on port `5173`, the backend/Swagger UI on `8080`, and the Keycloak admin console on `9090` (log in with `admin` / `password`).

### How Keycloak is already set up for you
The realm import at `docker/planmate/keycloak/planmate-realm.json` provisions everything the app needs — nobody has to click through the Keycloak admin console to get a working setup:
- **`planmate-frontend`** — a public client (PKCE flow, no secret) that the React app logs in through. Its redirect URI and web origin are fixed to `http://localhost:5173`, so login will break with Keycloak's `Invalid parameter: redirect_uri` page if you run the frontend on any other port.
- **`planmate-backend`** — a confidential client the backend uses as a service account to talk to Keycloak's admin API (looking up users, etc.). It needs a secret, which is where `backend.env` comes in: the realm's copy of the secret is `${KEYCLOAK_CLIENT_SECRET:planmate-dev-secret}` — a placeholder Keycloak resolves from the environment at import time, falling back to `planmate-dev-secret` if nothing is set. As long as `backend.env`'s `KEYCLOAK_CLIENT_SECRET` matches that default (it does, out of the box), the two sides agree automatically.
- **Realm roles `USER` and `ADMIN`** — every self-registered user is automatically granted `USER`. `ADMIN` is not granted automatically; if you need admin-only features on your own account, open the Keycloak admin console, find your user under the `planmate` realm, and add the `ADMIN` role to it under Role Mapping, then log out and back in so the new role ends up in your token.

### First login
Run the sample-users script (below), then log in at `http://localhost:5173` as **`admin` / `admin`** — it has both the `USER` and `ADMIN` roles. You can also register a new account from the frontend instead; self-registration is on, and you'll land with just the `USER` role, so grant yourself `ADMIN` as described above if you need admin-only features on it.

### Optional: sample users for testing
PlanMate splits expenses *between people*, so a single account isn't enough to try out groups, members, or settlements. Nothing is seeded in the realm itself — `planmate-realm.json` intentionally ships with no application users, so it's safe to import as-is in production. Locally, this script fills that gap:

```bash
./docker/planmate/dev-users.sh
```

This creates:
- **`admin`** — password `admin`, with both the `USER` and `ADMIN` roles.
- **10 ordinary users** — `alice`, `bob`, `carol`, `david`, `emma`, `frank`, `grace`, `henry`, `ivy`, `jack` — each with the `USER` role and **password equal to the username** (`alice` / `alice`, and so on).

Add them to a group by username in the UI and start logging expenses between them.

Notes:
- Only **Keycloak** needs to be running. The backend is optional: if it's up, the script also creates each user's app-side record immediately; if not, that record is created automatically the first time each user logs in.
- It's safe to re-run — users that already exist are skipped.

---
## Deployment

PlanMate runs in production entirely on free tiers, split across four providers:

| Piece | Where |
|---|---|
| Frontend (static site) | Render — `https://planmate-frontend.onrender.com` |
| Backend (Docker web service) | Render — `https://planmate-backend-y8gc.onrender.com` |
| Keycloak (Docker web service) | Render — `https://planmate-keycloak.onrender.com` |
| Postgres | Neon (`planmate` schema for the app, `keycloak` schema for auth) |
| Redis | Upstash |
| Container images | GitHub Container Registry (`ghcr.io`), tagged by commit SHA |

### CI/CD

- **`.github/workflows/ci.yml`** — runs on every PR into `main`. Backend: `./mvnw verify` (tests + Jacoco coverage gate). Frontend: lint + build. A monorepo path filter skips whichever half wasn't touched.
- **`.github/workflows/cd.yml`** — runs on every push to `main`. Calls `ci.yml` as a hard gate, builds and pushes changed images (backend, Keycloak) to GHCR tagged with the commit SHA, then hits each Render service's deploy hook in order — Keycloak, then backend, then frontend.