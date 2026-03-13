# Cloud Incident Lab + Agentic Ops Copilot

## What Is This?

A local cloud-simulated incident-response platform paired with an AI-powered SRE copilot.

The project is split into two worlds:

**World 1 — The system being monitored**
A small microservices app made of real containerized services that can fail in realistic ways.

**World 2 — The agent system**
An AI assistant that investigates those failures the same way a real SRE would.

Instead of a generic chatbot, the agent uses tools to:
- Inspect logs
- Inspect metrics
- Check service health
- Review deployment history
- Search runbooks
- Form hypotheses and rank root causes
- Recommend safe remediation steps

---

## The Real-World Story

Imagine a company web app with a frontend, backend API, database, and cache.

Something goes wrong:
- The website becomes slow
- Users get 500 errors
- A new deploy broke the app
- Redis went down
- The database is overloaded

Normally an SRE investigates by checking logs, metrics, health checks, recent deploys, and runbooks.

This project simulates that entire process — and your AI agent acts like a junior SRE assistant doing the investigation.

**One-line pitch:**
> "I built an AI-powered incident investigation platform for a cloud-style microservices system."

---

## Architecture

```
        Frontend (React)
               |
               v
         Backend API (Spring Boot)
         /              \
        v                v
   PostgreSQL           Redis
        |
        v
      Worker
```

The agent observes this system through tools and produces structured diagnoses.

---

## Services

| Service      | Tech          | Role                                   |
|--------------|---------------|----------------------------------------|
| Frontend     | React         | User-facing app, calls backend API     |
| Backend API  | Spring Boot   | Core service, connects to DB and cache |
| Database     | PostgreSQL    | Persistent storage                     |
| Cache        | Redis         | Caching layer to reduce DB load        |
| Worker       | Python        | Background job processor               |

---

## Tech Stack

| Layer            | Technology                        |
|------------------|-----------------------------------|
| Frontend         | React + TypeScript + Tailwind CSS |
| Core API         | Spring Boot (Java)                |
| Agent Service    | Python + FastAPI + LangGraph      |
| LLM              | Ollama (local model)              |
| Vector Store     | ChromaDB                          |
| Database         | PostgreSQL                        |
| Cache            | Redis                             |
| Metrics          | Prometheus + Grafana              |
| Logs             | Loki                              |
| Containerization | Docker + Docker Compose           |

---

## Folder Structure

```
cloud-incident-lab/
  frontend/              # React dashboard
  core-api/              # Spring Boot — incidents, services, deployments, approvals
  agent-service/         # Python FastAPI + LangGraph + tools
  services/
    backend-app/         # Monitored backend service
    frontend-app/        # Monitored frontend service
    worker/              # Background worker
  infra/
    docker/              # Docker Compose files
    prometheus/          # Prometheus config
    grafana/             # Grafana dashboards
    loki/                # Loki config
  runbooks/              # Markdown runbooks indexed by the agent
  incident-scenarios/    # Scripts that trigger failures
  scripts/               # Setup, reset, load test helpers
  docs/                  # This folder
```

---

## Database Schema

| Table               | Purpose                                       |
|---------------------|-----------------------------------------------|
| `services`          | Service metadata                              |
| `deployments`       | Deploy history per service                    |
| `incidents`         | Incident records                              |
| `incident_evidence` | Logs/metrics/runbook refs used in diagnosis   |
| `agent_runs`        | Each agent investigation attempt              |
| `approvals`         | User approval decisions for proposed actions  |
| `reports`           | Generated postmortem content                  |

---

## Why This Project Is Strong for Hiring

- Shows backend systems understanding (real services, real dependencies)
- Shows distributed/cloud concepts (failures across services, not just one script)
- Shows observability knowledge (logs, metrics, health signals)
- Shows proper agentic AI (LLM uses tools and makes decisions, not just memory)
- Shows safe AI design (human approval before risky actions)
- Shows product thinking (real use case, dashboard, measurable outputs)

---

## Resume Bullet

> Built an agentic incident-response platform for a cloud-simulated microservices environment using React, Spring Boot, FastAPI, PostgreSQL, Redis, Docker Compose, Prometheus, Grafana, and LangGraph. Designed tool-using AI workflows to analyze logs, metrics, deployment history, and runbooks, diagnose root causes, and recommend safe remediation steps across simulated production incidents.
