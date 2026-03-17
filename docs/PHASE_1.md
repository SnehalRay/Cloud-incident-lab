# Phase 1 — Build the Monitored System

**Goal:** Get the local microservice environment running, connected, and breakable on demand.

By the end of this phase you have a working multi-service app you can inspect and crash intentionally. No agent yet — just the system the agent will later investigate.

---

## Step 1 — Set Up the Project Structure

Create the folder skeleton so every service has a home before writing any code.

```
cloud-incident-lab/
  services/
    backend-app/       ← Spring Boot API
    frontend-app/      ← React frontend
    worker/            ← Python background worker
  infra/
    docker/            ← Docker Compose files
    postgres/          ← Init SQL scripts
    redis/             ← Redis config
  incident-scenarios/  ← Scripts that trigger failures
  scripts/             ← Setup and reset helpers
```

- [ ] Create the folder structure above
- [ ] Add a root-level `docker-compose.yml` that will wire everything together
- [ ] Add `.env.example` with placeholder values for all required env vars

---

## Step 2 — PostgreSQL Setup

Set up the database that the backend will use.

- [ ] Add `postgres` service to `docker-compose.yml`
  - Image: `postgres:15`
  - Expose port `5432`
  - Mount a named volume for data persistence
  - Set `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` from `.env`
- [ ] Create `infra/postgres/init.sql` with the initial schema:

```sql
CREATE TABLE services (
  id SERIAL PRIMARY KEY,
  name VARCHAR(100) NOT NULL UNIQUE,
  description TEXT,
  created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE deployments (
  id SERIAL PRIMARY KEY,
  service_name VARCHAR(100) NOT NULL,
  version VARCHAR(50),
  deployed_at TIMESTAMP DEFAULT NOW(),
  commit_message TEXT,
  config_diff TEXT,
  deployed_by VARCHAR(100) DEFAULT 'system'
);

CREATE TABLE incidents (
  id SERIAL PRIMARY KEY,
  title TEXT NOT NULL,
  status VARCHAR(50) DEFAULT 'open',
  severity VARCHAR(20) DEFAULT 'medium',
  started_at TIMESTAMP DEFAULT NOW(),
  resolved_at TIMESTAMP,
  summary TEXT
);
```

- [ ] Mount the init script so Postgres runs it on first start
- [ ] Verify Postgres starts: `docker compose up postgres`

---

## Step 3 — Redis Setup

Set up the cache layer.

- [ ] Add `redis` service to `docker-compose.yml`
  - Image: `redis:7-alpine`
  - Expose port `6379`
  - Mount a named volume for persistence
- [ ] Create `infra/redis/redis.conf` with basic config:
  - Set `maxmemory 256mb`
  - Set `maxmemory-policy allkeys-lru`
- [ ] Verify Redis starts: `docker compose up redis`
- [ ] Smoke test: `docker exec -it redis redis-cli ping` should return `PONG`

---

## Step 4 — Backend API Service (Spring Boot)

This is the core service. It connects to Postgres and Redis and is the main source of incidents.

### 4.1 — Bootstrap the Project

- [ ] Generate a new Spring Boot project (Spring Initializr or your IDE)
  - Dependencies: Spring Web, Spring Data JPA, Spring Actuator, Lombok, PostgreSQL Driver, Spring Data Redis
- [ ] Place it in `services/backend-app/`
- [ ] Configure `application.yml` to read DB and Redis connection from environment variables:

```yaml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USER}
    password: ${DB_PASSWORD}
  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT}
  jpa:
    hibernate:
      ddl-auto: validate

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
```

### 4.2 — Endpoints to Implement

- [ ] `GET /health` — returns service status (also exposed via Actuator)
- [ ] `GET /api/items` — returns a list of items (queries Postgres, caches in Redis)
- [ ] `POST /api/items` — creates a new item (writes to Postgres, invalidates cache)
- [ ] `GET /api/status` — returns a JSON summary of DB and Redis connectivity

### 4.3 — Health Check Logic

- [ ] Check Postgres connectivity
- [ ] Check Redis connectivity
- [ ] Return combined status: `healthy` / `degraded` / `unhealthy`

### 4.4 — Structured Logging

- [ ] Add Logback or SLF4J logging with JSON format (use `logstash-logback-encoder`)
- [ ] Log every request with: service name, endpoint, duration, status code
- [ ] Log connection failures explicitly so they're easy to find

### 4.5 — Add to Docker Compose

- [ ] Add `backend` service to `docker-compose.yml`
  - Build from `services/backend-app/Dockerfile`
  - Expose port `8080`
  - Pass `DB_URL`, `DB_USER`, `DB_PASSWORD`, `REDIS_HOST`, `REDIS_PORT` from `.env`
  - Add `depends_on: [postgres, redis]`
- [ ] Write `services/backend-app/Dockerfile`

```dockerfile
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY . .
RUN ./mvnw package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 4.6 — Rate Limiter *(additional)*

Per-instance rate limiting on `POST /api/items` using Redis as shared state. Each frontend instance is identified by an `X-Instance-ID` header.

- [ ] Read `X-Instance-ID` header from every `POST /api/items` request
- [ ] Implement a sliding window counter in Redis — key pattern: `rate_limit:<instance-id>`
- [ ] Limit: **2 requests per 3 seconds per instance**
- [ ] Return `429 Too Many Requests` when exceeded
- [ ] On violation, push a job to `jobs:queue`:
  ```json
  {
    "type": "rate_limit_violation",
    "instance_id": "instance-a",
    "endpoint": "POST /api/items",
    "timestamp": "2026-03-17T10:00:00Z"
  }
  ```

---

## Step 5 — Frontend Service (React)

A minimal app that calls the backend. Failures in the backend will be visible here.

### 5.1 — Bootstrap

- [ ] Create React + TypeScript app in `services/frontend-app/`
  - Use Vite: `npm create vite@latest frontend-app -- --template react-ts`
  - Install Tailwind CSS

### 5.2 — Pages to Implement

- [ ] **Home page** — shows a list of items fetched from `GET /api/items`
- [ ] **Status page** — shows backend and its dependencies (DB, Redis) health

### 5.3 — Error Handling

- [ ] If the backend returns an error, display a clear error state (not a blank page)
- [ ] Log fetch errors to the browser console with timestamps

### 5.4 — Add to Docker Compose

- [ ] Add `frontend` service to `docker-compose.yml`
  - Build from `services/frontend-app/Dockerfile`
  - Expose port `3000`
  - Pass `VITE_API_URL` so the frontend knows where the backend is

```dockerfile
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
```

### 5.5 — Instance ID & 429 Handling *(additional)*

- [ ] Assign a hardcoded `X-Instance-ID` per running frontend instance (e.g. `instance-a`, `instance-b`, `instance-c`)
- [ ] Send the header on every `POST /api/items` request
- [ ] Handle `429` responses with a clear UI message: *"Too many requests — slow down"*
- [ ] Log the rejection to the browser console with a timestamp

---

## Step 6 — Worker Service (Rust)

A Rust background worker that consumes jobs from Redis and writes audit records to Postgres.

### 6.1 — Bootstrap

- [ ] Create `services/worker/` as a Rust project (`cargo init`)
- [ ] Dependencies: `tokio`, `redis`, `sqlx`, `serde`, `serde_json`

### 6.2 — Implement

- [ ] Connect to Redis and block on `BLPOP jobs:queue`
- [ ] Deserialize job payload and handle type `rate_limit_violation`
- [ ] Add `audit_log` table to `infra/postgres/init.sql`:
  ```sql
  CREATE TABLE audit_log (
    id SERIAL PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL,
    instance_id VARCHAR(100),
    endpoint VARCHAR(200),
    created_at TIMESTAMP DEFAULT NOW()
  );
  ```
- [ ] Write one row to `audit_log` per processed job
- [ ] Loop back and wait for the next job
- [ ] Create `services/worker/Dockerfile`

```dockerfile
FROM rust:1.77-alpine AS build
WORKDIR /app
COPY . .
RUN cargo build --release

FROM alpine:3.19
WORKDIR /app
COPY --from=build /app/target/release/worker .
CMD ["./worker"]
```

### 6.3 — Add to Docker Compose

- [ ] Add `worker` service to `docker-compose.yml`
  - Pass `REDIS_URL`, `DATABASE_URL` from `.env`
  - Add `depends_on: [postgres, redis]`

---

## Step 7 — Wire Everything with Docker Compose

Finalize the root `docker-compose.yml` so the entire system starts with one command.

- [ ] All 5 services defined: `postgres`, `redis`, `backend`, `frontend`, `worker`
- [ ] All services on a shared Docker network (`incident-lab-network`)
- [ ] Named volumes defined for Postgres and Redis data
- [ ] All secrets passed via `.env` (never hardcoded)
- [ ] Health checks defined for `postgres` and `redis` so dependent services wait properly

```yaml
# Minimal structure reference
services:
  postgres:
    image: postgres:15
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${DB_USER}"]
      interval: 10s
      retries: 5

  redis:
    image: redis:7-alpine
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      retries: 5

  backend:
    build: ./services/backend-app
    depends_on:
      postgres:
        condition: service_healthy
      redis:
        condition: service_healthy

  frontend:
    build: ./services/frontend-app
    depends_on:
      - backend

  worker:
    build: ./services/worker
    depends_on:
      postgres:
        condition: service_healthy
      redis:
        condition: service_healthy
```

- [ ] Run `docker compose up --build` and verify all services start without errors
- [ ] Verify frontend loads at `http://localhost:3000`
- [ ] Verify backend health at `http://localhost:8080/health`
- [ ] Verify backend returns items at `http://localhost:8080/api/items`

---

## Step 8 — Seed Data

Create a script that populates the DB with realistic data to make incidents meaningful.

- [ ] Create `scripts/seed.sql` or `scripts/seed.py`:
  - Insert rows into `services` table for each running service
  - Insert a few sample `deployments` records with realistic timestamps and commit messages
  - Insert sample items into whatever app table the backend uses
- [ ] Run seed: `docker exec -i postgres psql -U $DB_USER -d $DB_NAME < scripts/seed.sql`
- [ ] Verify data exists in Postgres

---

## Step 9 — Write the Reset Script

You need to be able to restore the system to a clean healthy state after triggering incidents.

- [ ] Create `scripts/reset.sh`:
  - Stop and remove all containers
  - Remove named volumes (clears DB and Redis data)
  - Rebuild and restart all services
  - Re-run seed data

```bash
#!/bin/bash
docker compose down -v
docker compose up --build -d
sleep 5
# re-seed
docker exec -i postgres psql -U "$DB_USER" -d "$DB_NAME" < scripts/seed.sql
echo "System reset complete."
```

- [ ] Make it executable: `chmod +x scripts/reset.sh`
- [ ] Test it: trigger a failure, then run reset, verify everything is healthy again

---

## Step 10 — Implement and Test Incident Scenarios

Now intentionally break the system to confirm everything behaves as expected.

### Scenario 1: Redis Outage

- [ ] Create `incident-scenarios/redis-outage.sh`:
  ```bash
  docker stop cloud-incident-lab-redis-1
  ```
- [ ] Verify: backend logs show `Redis connection refused`
- [ ] Verify: `GET /api/items` still works (fallback to DB) but is slower
- [ ] Verify: `GET /health` shows Redis as unhealthy
- [ ] Reset with `scripts/reset.sh`

### Scenario 2: Bad Deploy

- [ ] Create `incident-scenarios/bad-deploy.sh`:
  ```bash
  # Override REDIS_HOST with a wrong value and restart backend
  docker compose stop backend
  REDIS_HOST=wrong-host docker compose up -d backend
  # Write a fake deployment record to DB
  docker exec -i postgres psql -U "$DB_USER" -d "$DB_NAME" \
    -c "INSERT INTO deployments (service_name, version, commit_message, config_diff) VALUES ('backend', 'v1.1.0', 'Update cache config', 'REDIS_HOST changed');"
  ```
- [ ] Verify: backend logs show Redis connection failures after deploy record timestamp
- [ ] Verify: health check shows backend degraded
- [ ] Reset with `scripts/reset.sh`

### Scenario 3: DB Overload

- [ ] Create `incident-scenarios/db-overload.sh`:
  ```bash
  # Run a slow query loop to spike DB CPU
  docker exec -i postgres psql -U "$DB_USER" -d "$DB_NAME" \
    -c "SELECT pg_sleep(10);" &
  ```
- [ ] Verify: backend response times increase
- [ ] Verify: backend logs show slow query warnings or timeouts
- [ ] Reset with `scripts/reset.sh`

### Scenario 4: Backend Crash Loop

- [ ] Add a hidden endpoint to the backend: `GET /api/debug/crash` — throws an uncaught exception
- [ ] Create `incident-scenarios/backend-crash.sh`:
  ```bash
  curl http://localhost:8080/api/debug/crash
  ```
- [ ] Verify: container restarts (watch with `docker compose ps`)
- [ ] Verify: health check fails during restart window
- [ ] Reset with `scripts/reset.sh`

### Scenario 5: Worker Backlog

- [ ] Create `incident-scenarios/worker-backlog.sh`:
  ```bash
  # Stop the worker so the queue builds up
  docker compose stop worker
  # Push 100 fake jobs into the queue
  docker exec -i redis redis-cli RPUSH jobs:queue $(python3 -c "print(' '.join(['job-'+str(i) for i in range(100)]))")
  ```
- [ ] Verify: Redis queue length increases (`LLEN jobs:queue`)
- [ ] Verify: worker health shows stopped
- [ ] Reset with `scripts/reset.sh`

---

## Phase 1 Done — Checklist

- [ ] All 5 services start with `docker compose up --build`
- [ ] Frontend loads and displays data from backend
- [ ] Backend `/health` reflects real DB and Redis state
- [ ] Seed data exists in Postgres
- [ ] All 5 incident scenarios can be triggered and reset
- [ ] Logs are structured and visible via `docker compose logs`
- [ ] Reset script works cleanly

**You are ready for Phase 2 (Observability) when all items above are checked.**
