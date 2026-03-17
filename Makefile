# ─────────────────────────────────────────────────────────────
# Cloud Incident Lab — Makefile
# Usage: make <target>
# ─────────────────────────────────────────────────────────────

.PHONY: up down destroy reset nuke logs ps \
        ping-redis psql \
        start-infra start-spring start-frontend \
        help

# ── Default target ────────────────────────────────────────────
help:
	@echo ""
	@echo "  Cloud Incident Lab"
	@echo ""
	@echo "  make up          Start all services (detached)"
	@echo "  make down        Stop and remove containers (keeps volumes + images)"
	@echo "  make reset       Stop, wipe data volumes, rebuild, and start fresh"
	@echo "  make nuke        reset + also delete pulled images (full clean slate)"
	@echo "  make destroy     Delete everything and stop — no restart"
	@echo ""
	@echo "  make logs        Tail logs for all services"
	@echo "  make ps          Show container status and health"
	@echo ""
	@echo "  make ping-redis  Smoke test Redis (should return PONG)"
	@echo "  make psql        Open a psql shell inside the Postgres container"
	@echo ""
	@echo "  make start-infra    Start only Postgres + Redis (for local Spring dev)"
	@echo "  make start-spring   Run Spring Boot locally against Docker infra"
	@echo "  make start-frontend Run the React frontend dev server (localhost:5173)"
	@echo ""

# ── Start ─────────────────────────────────────────────────────
up:
	docker compose up -d --build
	@echo ""
	@echo "Services started. Run 'make ps' to check health."

# ── Stop (keeps volumes and images) ──────────────────────────
down:
	docker compose down

# ── Reset: wipe data, rebuild, restart ───────────────────────
# Removes containers + named volumes (DB and Redis data is cleared).
# Images are kept so the rebuild only recompiles changed layers.
reset:
	docker compose down -v
	docker compose up -d --build
	@echo ""
	@echo "Reset complete. Run 'make ps' to check health."

# ── Destroy: delete everything, do NOT restart ───────────────
# Use this when you want to walk away and come back later.
# Run 'make up' whenever you're ready to start again.
destroy:
	docker compose down -v --rmi all
	@echo ""
	@echo "Everything deleted. Run 'make up' when you're ready to start again."

# ── Nuke: full clean slate ────────────────────────────────────
# Removes containers, volumes, AND pulled images.
# Next 'make up' will re-pull postgres and redis from Docker Hub.
nuke:
	docker compose down -v --rmi all
	docker compose up -d --build
	@echo ""
	@echo "Nuke + reinit complete. Run 'make ps' to check health."

# ── Status ────────────────────────────────────────────────────
ps:
	docker compose ps

# ── Logs ──────────────────────────────────────────────────────
logs:
	docker compose logs -f

# ── Local Spring Boot dev ────────────────────────────────────
# Starts only Postgres + Redis in Docker, then runs the Spring Boot app
# directly on your machine (no Docker build needed — fast iteration).
#
# DB_URL is overridden to use localhost:5433 because that's the host-
# machine port that docker-compose maps from the container's 5432.
start-infra:
	docker compose up -d postgres redis
	@echo ""
	@echo "Postgres (localhost:5433) and Redis (localhost:6379) are up."

start-spring: start-infra
	@echo ""
	@echo "Starting Spring Boot locally..."
	cd services/backend-app/backend-spring && \
	  DB_URL=jdbc:postgresql://localhost:5433/$${POSTGRES_DB:-incidentlab} \
	  DB_USER=$${POSTGRES_USER:-incidentuser} \
	  DB_PASSWORD=$${POSTGRES_PASSWORD:-changeme} \
	  REDIS_HOST=localhost \
	  REDIS_PORT=6379 \
	  ./mvnw spring-boot:run

# ── Local frontend dev ────────────────────────────────────────
start-frontend:
	@echo ""
	@echo "Starting React dev server at http://localhost:5173 ..."
	cd services/frontend-app/template && npm run dev

# ── Smoke tests ───────────────────────────────────────────────
ping-redis:
	docker exec -it incident-lab-redis redis-cli ping

psql:
	docker exec -it incident-lab-postgres psql -U $${POSTGRES_USER:-incidentuser} -d $${POSTGRES_DB:-incidentlab}
