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
- Docker + Docker Compose

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
- **`planmate-frontend`** — a public client (PKCE flow, no secret) that the React app logs in through. Its redirect URI and web origin are fixed to `http://localhost:5173`, so login will break with an `invalid_redirect_uri` error if you run the frontend on a different port.
- **`planmate-backend`** — a confidential client the backend uses as a service account to talk to Keycloak's admin API (looking up users, etc.). It needs a secret, which is where `backend.env` comes in: the realm's copy of the secret is `${KEYCLOAK_CLIENT_SECRET:planmate-dev-secret}` — a placeholder Keycloak resolves from the environment at import time, falling back to `planmate-dev-secret` if nothing is set. As long as `backend.env`'s `KEYCLOAK_CLIENT_SECRET` matches that default (it does, out of the box), the two sides agree automatically.
- **Realm roles `USER` and `ADMIN`** — every self-registered user is automatically granted `USER`. `ADMIN` is not granted automatically; if you need admin-only features, open the Keycloak admin console, find your user under the `planmate` realm, and add the `ADMIN` role to it under Role Mapping, then log out and back in so the new role ends up in your token.

### First login
Register a new account directly from the frontend — self-registration is on, and you'll land with the `USER` role already assigned.
