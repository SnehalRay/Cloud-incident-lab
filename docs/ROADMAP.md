# Roadmap

## Objectives

### Primary Objective
Build a local cloud-simulated incident-response platform where an AI agent can diagnose production-style failures using logs, metrics, deployment history, and runbooks — and explain its reasoning with evidence.

### Secondary Objectives
- Demonstrate agentic AI design (tool use, multi-step reasoning, structured output)
- Demonstrate observability and ops engineering knowledge
- Demonstrate microservices and distributed systems understanding
- Produce a portfolio project strong enough to mention in interviews

---

## Timeline Overview

| Week | Focus                         | Milestone                                      |
|------|-------------------------------|------------------------------------------------|
| 1    | Monitored system + incidents  | Working multi-service app with 2+ failures     |
| 2    | Agent tools + workflow        | Agent produces real diagnoses                  |
| 3    | Dashboard + polish            | Fully demoable, portfolio-ready                |

---

## Week 1 — The System

**Goal:** Build the environment the agent will monitor.

- [ ] All services running in Docker Compose
- [ ] Services connected (backend → DB, backend → Redis, frontend → backend)
- [ ] Health endpoints on each service
- [ ] Prometheus metrics exposed
- [ ] Grafana and Loki set up
- [ ] Deployment history tracked in DB
- [ ] At least 2 incident scenarios reproducible

**Milestone:** You can trigger a Redis outage and watch Grafana show the degradation.

---

## Week 2 — The Agent

**Goal:** Build the intelligence layer.

- [ ] FastAPI agent service running
- [ ] All 5 core tools implemented and returning real data
- [ ] LangGraph workflow with intake → plan → evidence → synthesis → recommendation
- [ ] Ollama local model connected
- [ ] ChromaDB with indexed runbooks
- [ ] Agent returns structured JSON diagnosis for at least 3 scenarios

**Milestone:** You ask "Why is the backend slow?" and the agent correctly identifies Redis as the root cause.

---

## Week 3 — The Product

**Goal:** Make it look and feel like a real tool.

- [ ] React dashboard with all panels (status, ask agent, evidence, recommendations)
- [ ] Approval modal for risky actions
- [ ] Postmortem generation and display
- [ ] All 5 incident scenarios covered and tested
- [ ] Architecture diagram
- [ ] Demo script written
- [ ] Demo video recorded

**Milestone:** End-to-end live demo works cleanly from browser open to postmortem displayed.

---

## Phase Checkpoints

### After Phase 1
You can run the app and break it.

### After Phase 2
You can observe the breakage in Grafana and Loki.

### After Phase 3
You can trigger any incident with a single script and reset it.

### After Phase 4
Tools return real data — logs from Loki, metrics from Prometheus, deploys from DB.

### After Phase 5
The agent produces a structured, evidence-based diagnosis with ranked root causes.

### After Phase 6
A user can ask a question and see the full diagnosis in the browser.

### After Phase 7
After each investigation, a postmortem report is auto-generated.

### After Phase 8
The project is demo-ready and portfolio-ready.

---

## MVP Definition

The MVP is done when:

> A user can open the dashboard, see a degraded service, ask the agent why it's failing, and receive a structured diagnosis with evidence and recommended next steps.

That means:
- At least 4 services running
- At least 4 incident scenarios
- At least 5 agent tools working
- Agent produces: summary + evidence + root cause + confidence + recommendations
- Dashboard shows the diagnosis

Nothing else is required for MVP.

---

## Metrics for Success

| Metric                            | Target                              |
|-----------------------------------|-------------------------------------|
| Incident scenarios covered        | 5                                   |
| Agent tools implemented           | 7+                                  |
| Correct root cause diagnoses      | 4/5 scenarios diagnosed correctly   |
| Time to diagnosis (agent runtime) | Under 60 seconds per investigation  |
| Demo length                       | 2 minutes, clean walkthrough        |

---

## What Not to Build (Yet)

- More than 5 services
- Kubernetes / Helm charts
- Authentication / multi-user
- External API dependencies
- Real cloud infrastructure
- Production-grade security

Keep scope tight. MVP first, stretch later.
