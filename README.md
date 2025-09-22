LedgerPro

Small full-stack app to manage accounts and ledger entries.
Backend: Spring Boot 3 / Java 21 with JWT + Spring Security, PostgreSQL, Flyway.
Frontend: React + Vite (TypeScript) proxied to /api.

🧱 Tech Stack

Backend: Spring Boot 3.5, Spring Web, Spring Security, Spring Data JPA, Flyway, Springdoc OpenAPI

Auth: JWT (jjwt 0.11.x)

DB: PostgreSQL 16.x (HikariCP)

Frontend: React 18, Vite

Build: Maven, Node 20+

📁 Project Structure
/                    # backend (Spring Boot)
  ├─ src/main/java/com/sofi/ledgerpro/...
  ├─ src/main/resources/
  │   ├─ application.yml
  │   └─ db/migration/         # Flyway scripts (V1__, V2__, ...)
  └─ ui/                       # frontend (Vite + React)

🚀 Getting Started (Local)
1) Prerequisites

Java 21

Maven 3.9+

Node 20+ and npm

Docker (for Postgres)

2) Database (Docker)
# Create a local PostgreSQL 16 instance
docker run --name pg-ledger \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=appdb \
  -p 5432:5432 -d postgres:16


If the container name already exists: docker rm -f pg-ledger and run the command again.

3) Backend configuration

src/main/resources/application.yml (defaults for local dev):

DB URL: jdbc:postgresql://localhost:5432/appdb

DB user/password: postgres / postgres

JWT: set jwt.secret (a Base64-encoded 256-bit key). A dev value is already provided.

Errors are configured not to expose stack traces or internal messages.

4) Flyway migrations

If you change migration scripts or need to fix checksums, run:

bash/macOS/Linux

# migrate
mvn -q \
  -Dflyway.user=postgres \
  -Dflyway.password=postgres \
  -Dflyway.url=jdbc:postgresql://localhost:5432/appdb \
  org.flywaydb:flyway-maven-plugin:11.7.2:migrate

# if you see "checksum mismatch", repair then migrate:
mvn -q \
  -Dflyway.user=postgres \
  -Dflyway.password=postgres \
  -Dflyway.url=jdbc:postgresql://localhost:5432/appdb \
  org.flywaydb:flyway-maven-plugin:11.7.2:repair


Windows PowerShell

# migrate
mvn -q `
  "-Dflyway.user=postgres" `
  "-Dflyway.password=postgres" `
  "-Dflyway.url=jdbc:postgresql://localhost:5432/appdb" `
  org.flywaydb:flyway-maven-plugin:11.7.2:migrate

# repair if needed
mvn -q `
  "-Dflyway.user=postgres" `
  "-Dflyway.password=postgres" `
  "-Dflyway.url=jdbc:postgresql://localhost:5432/appdb" `
  org.flywaydb:flyway-maven-plugin:11.7.2:repair

5) Run the backend

It’s recommended to run in UTC:

bash / PowerShell

mvn -q -D"spring-boot.run.jvmArguments=-Duser.timezone=UTC" spring-boot:run


Backend will be available at http://localhost:8080.

6) Run the frontend
cd ui
npm install
npm run dev


Vite serves http://localhost:5173 and proxies API calls to the backend.

ui/vite.config.ts sets:

server: {
  port: 5173,
  proxy: { '/api': { target: 'http://localhost:8080', changeOrigin: true } }
}

🔐 Authentication

Endpoints:

POST /auth/signup – register a user

POST /auth/login – returns a JWT

The frontend stores the token and sends it as:

Authorization: Bearer <jwt>

Data isolation

Accounts are scoped per user via accounts.owner_id (FK to users.id).
Each authenticated user only sees and operates on their own accounts and entries.

📚 API Overview

Interactive docs (Swagger):
http://localhost:8080/swagger-ui.html (or /swagger-ui/index.html)

GET /api/accounts → list your accounts

POST /api/accounts → create an account

{ "name": "Savings", "initialBalance": 1000.00 }


GET /api/accounts/{id}/entries → list entries (pagination with page, size)

POST /api/accounts/{id}/entries → add an entry

{ "amount": 250.00, "kind": "CREDIT" } // or "DEBIT"


POST /api/accounts/transfer → transfer between two accounts you own

{ "from": "UUID", "to": "UUID", "amount": 150.00 }

🖥️ Frontend Notes

API client (ui/src/api.ts) automatically attaches Authorization if a token exists and maps common error messages for better UX.

On 4xx/5xx, the backend hides internal stack traces and returns safe error payloads (see application.yml).

🧪 Quick cURL
# signup
curl -sX POST http://localhost:8080/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"secret"}'

# login (capture token with jq)
TOKEN=$(curl -sX POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"secret"}' | jq -r .token)

# create account
curl -sX POST http://localhost:8080/api/accounts \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"name":"Checking","initialBalance":5000}'

🛠️ Troubleshooting

“container name … already in use”
Remove the old container: docker rm -f pg-ledger, then re-create it.

403/401 in the UI
Make sure you logged in and the token is set; the client sends Authorization: Bearer ….

Flyway checksum mismatch
Run repair then migrate (see Flyway section).

Timezone issues
Run the app with -Duser.timezone=UTC (see backend run command).
