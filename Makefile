# ─────────────────────────────────────────────────────────────
# Cloud Incident Lab — Makefile
# Usage: make <target>
# ─────────────────────────────────────────────────────────────

.PHONY: up down destroy reset nuke logs ps \
        ping-redis psql \
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

# ── Smoke tests ───────────────────────────────────────────────
ping-redis:
	docker exec -it incident-lab-redis redis-cli ping

psql:
	docker exec -it incident-lab-postgres psql -U $${POSTGRES_USER:-incidentuser} -d $${POSTGRES_DB:-incidentlab}
