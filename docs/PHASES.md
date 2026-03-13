# Implementation Phases

## Phase 1 — Build the Monitored System

**Goal:** Get the local microservice environment running and breakable.

### Tasks
- [ ] Create backend service (Spring Boot) with `/health`, `/metrics`, basic API endpoints
- [ ] Connect PostgreSQL — schema, connection pool, basic queries
- [ ] Connect Redis — cache layer for backend responses
- [ ] Create minimal frontend service (React) that calls backend
- [ ] Create worker service for background jobs
- [ ] Wire all services together with Docker Compose
- [ ] Verify services can talk to each other

**Output:** A small working microservice app you can break on demand.

---

## Phase 2 — Add Observability

**Goal:** Make the system inspectable before the agent exists.

### Tasks
- [ ] Expose Prometheus metrics endpoints on each service (latency, error rate, CPU, memory, cache hit rate, queue length)
- [ ] Set up Prometheus scrape config
- [ ] Set up Grafana with dashboards per service
- [ ] Aggregate logs with Loki
- [ ] Store deployment history in `deployments` DB table
- [ ] Add a script to simulate a deploy (writes a deploy record + restarts a service)

**Output:** You can inspect services through Grafana and logs even without the agent.

---

## Phase 3 — Create Incident Scenarios

**Goal:** Reproducible failures that the agent can investigate.

### Tasks
- [ ] Scenario 1: Redis outage (`docker stop redis`)
- [ ] Scenario 2: Bad deploy (redeploy backend with wrong env var)
- [ ] Scenario 3: DB overload (script that runs heavy queries)
- [ ] Scenario 4: Backend crash loop (introduce a runtime exception)
- [ ] Scenario 5: Worker backlog (pause worker container)
- [ ] Write a reset script that restores everything to healthy state

**Output:** You can trigger and reset any incident on demand.

---

## Phase 4 — Build Agent Tools

**Goal:** Expose real system data as callable tools for the LLM.

### Tasks
- [ ] `get_service_health(service)` — query health endpoint or DB
- [ ] `get_logs(service, time_window)` — query Loki or Docker logs
- [ ] `get_metrics(service, time_window)` — query Prometheus
- [ ] `get_recent_deployments(service)` — query `deployments` table
- [ ] `search_runbooks(query)` — vector search over `/runbooks/` markdown files using ChromaDB
- [ ] `get_dependencies(service)` — return static or DB-stored dependency map
- [ ] `get_incident_history(issue)` — query past incidents from DB

**Output:** The LLM has access to real evidence from the running system.

---

## Phase 5 — Build the Agent Workflow

**Goal:** Make the agent reason in structured steps rather than just prompting.

### Tasks
- [ ] Set up FastAPI agent service with LangGraph
- [ ] Connect to Ollama local model
- [ ] Build intake node (parse question, classify incident type, identify target service)
- [ ] Build planning node (decide which tools to call and in what order)
- [ ] Build evidence collection node (execute tool calls, store results)
- [ ] Build synthesis node (combine evidence into ranked hypotheses)
- [ ] Build recommendation node (generate safe next steps with confidence)
- [ ] Return structured JSON diagnosis to caller

**Output:** Agent produces evidence-based, structured diagnosis.

---

## Phase 6 — Build the Dashboard

**Goal:** The project becomes fully demoable.

### Tasks
- [ ] Service status cards (healthy / degraded / unhealthy, latency, error rate, last deploy)
- [ ] Active incident panel
- [ ] Ask-the-agent input box
- [ ] Evidence panel (logs excerpt, metrics chart, deploy timing, runbook snippet)
- [ ] Recommendation panel (top hypothesis, confidence, next steps)
- [ ] Approval modal for risky proposed actions
- [ ] Incident history list

**Output:** A user can open the app, see a broken service, ask why, and get a clear answer.

---

## Phase 7 — Report Generation

**Goal:** Make the platform feel like a real ops tool.

### Tasks
- [ ] Auto-generate incident timeline from evidence collected
- [ ] Generate root cause summary paragraph
- [ ] Generate action item list
- [ ] Store report in `reports` table
- [ ] Display postmortem on a dedicated incident detail page

**Output:** After each investigation, a structured postmortem is available.

---

## Phase 8 — Polish for Portfolio

**Goal:** Make it interview-ready and visually presentable.

### Tasks
- [ ] Architecture diagram (draw.io or Excalidraw)
- [ ] Screenshots of dashboard, evidence panel, diagnosis output
- [ ] Demo script (step-by-step walkthrough of one incident scenario)
- [ ] Clean up Docker Compose so the whole project starts with one command
- [ ] Record a 2-minute demo video
- [ ] Final resume bullet

**Output:** Portfolio-ready project you can demo live in interviews.
