# Cloud-incident-lab

## Setup

```bash
cp .env.example .env
```

## Commands

| Command | Description |
|---|---|
| `make up` | Start all services |
| `make down` | Stop containers (keeps data and images) |
| `make reset` | Wipe data, rebuild, and restart fresh |
| `make nuke` | Full clean slate — removes containers, data, and images, then restarts |
| `make destroy` | Delete everything and stop — no restart |
| `make ps` | Show container status and health |
| `make logs` | Tail logs for all services |
| `make ping-redis` | Smoke test Redis (should return PONG) |
| `make psql` | Open a psql shell inside the Postgres container |

## Ports

| Service | Host port |
|---|---|
| Postgres | `5433` (5432 is reserved for local install) |
| Redis | `6379` |
| Backend | `8080` |
| Frontend | `3000` |
