# Features

## MVP Features

### Microservice System
- 4–5 services running via Docker Compose
- Each service has health endpoints, logs, and metrics exposed

### Observability
- Prometheus metrics collection
- Grafana dashboards per service
- Loki log aggregation
- Deployment history stored in DB

### Incident Scenarios (at least 4)
- Redis outage
- Bad deploy (wrong env var)
- DB connection overload
- Backend crash loop
- Worker backlog

### Agent Tools
- `get_service_health(service)` — status, restart count, last heartbeat
- `get_logs(service, window)` — recent log lines
- `get_metrics(service, window)` — latency, error rate, CPU, memory
- `get_recent_deployments(service)` — deploy history and config changes
- `search_runbooks(query)` — vector search over markdown runbooks
- `get_dependencies(service)` — service dependency map
- `get_incident_history(issue)` — similar past incidents
- `suggest_action(context)` — safe remediation steps

### Agent Workflow (LangGraph)
- Intake node: understand question, classify incident type
- Planning node: decide which tools to use and in what order
- Evidence collection node: run tools
- Synthesis node: combine evidence into ranked hypotheses
- Recommendation node: generate safe next steps

### Agent Output (structured)
- Incident summary
- Evidence list (logs, metrics, deploy timing)
- Likely root cause
- Confidence level
- Recommended next steps

### Dashboard
- Service status cards (healthy / degraded / unhealthy)
- Ask-the-agent input
- Evidence panel
- Recommendation panel
- Incident history list

---

## Stretch Features

### Human Approval Flow
- Approval modal before any risky action executes (e.g. restart service)
- Approval/rejection stored in DB

### Incident Postmortem Generator
- Timeline summary
- Root cause summary
- Action items
- Exportable report

### Historical Incident Browser
- Browse past incidents
- View full evidence and diagnosis for each

### Agent Evaluation Dashboard
- Accuracy of diagnoses
- Tool call count per investigation
- Time to diagnosis

### Distributed Tracing
- Jaeger integration for request tracing across services

### Authentication
- User login/roles for dashboard access

### Cloud Deployment
- Deploy to Render / Railway / EC2 / ECS

---

## Incident Scenarios Detail

### Scenario 1: Redis Outage
**Trigger:** `docker stop redis`

What happens:
- Backend logs: `Redis connection refused`
- Cache hit rate drops
- DB CPU spikes
- Backend latency increases

Agent should conclude:
> Redis unavailable → cache misses → DB overloaded → latency spike

---

### Scenario 2: Bad Deploy
**Trigger:** Deploy backend with incorrect `REDIS_URL` env var

What happens:
- Backend cannot connect to Redis after deploy
- Error logs appear immediately after deploy timestamp
- Deploy history shows recent change

Agent should conclude:
> Config error in latest deploy caused Redis connection failure

---

### Scenario 3: DB Connection Overload
**Trigger:** Run heavy queries or simulate connection leak

What happens:
- DB CPU at 95%
- Logs: `too many clients`, `query timeout`
- Backend returns 500s

Agent should conclude:
> Database bottleneck or connection pool exhausted

---

### Scenario 4: Backend Crash Loop
**Trigger:** Introduce a runtime exception in backend

What happens:
- Container restart count increases
- Health checks fail
- Logs show exception on startup or request handling

Agent should conclude:
> Service unstable — logs reveal runtime exception as root cause

---

### Scenario 5: Worker Backlog
**Trigger:** Pause worker container

What happens:
- Job queue length increases
- Worker health shows stopped/unhealthy
- No job processing logs

Agent should conclude:
> Worker failure causing growing job backlog

---

## Agent Output Example

Given user question: *"Why is the backend slow?"*

```
Summary
-------
Backend latency increased after the 7:42 PM deploy.

Root Cause
----------
Redis misconfiguration in latest deploy caused cache misses,
increased DB load, and spiked response times.

Evidence
--------
- Backend logs: Redis connection refused (starting 7:42 PM)
- Cache hit rate: dropped from 85% → 5%
- DB CPU: increased from 35% → 91%
- Latency: increased from 150ms → 2500ms
- Deploy history: backend deploy at 7:42 PM changed REDIS_URL

Confidence: HIGH

Recommended Actions
-------------------
1. Verify REDIS_URL env var in latest deploy config
2. Rollback backend deploy if issue persists
3. Restart Redis after approval (requires confirmation)
```
