# DB Admin Assistant

An AI-powered database administrator assistant for PostgreSQL. It continuously monitors slow
queries and instance health metrics, uses an LLM (OpenAI via Spring AI) to explain performance
problems and draft remediations (indexes, config changes), and **never applies a change without
explicit administrator approval**. Every recommendation and every action on it is recorded in an
immutable audit trail.

## Architecture

Hexagonal architecture (ports & adapters) + DDD, single Maven module.

```
domain/            Pure Java — zero Spring/JPA imports. Aggregates, value objects, domain events,
                    ports (in = use cases, out = repositories/external systems).
application/        Use-case orchestration, Spring-managed. Command pattern for DDL execution,
                    domain-event listeners for audit logging.
infrastructure/      Everything framework-specific: JPA persistence adapters, JWT security,
                    Postgres metrics collector, Spring AI adapter, REST controllers, scheduler.
```

Key design patterns: **Strategy** (per-engine metrics collector), **Command** (`CreateIndexCommand`
/ `UpdateConfigParameterCommand`, built at draft time, invoked only after approval), **Factory**
(`RecommendationFactory` — Anti-Corruption Layer between the AI's raw output and the domain),
**Observer** (domain events → `AuditLogEventListener`), **Decorator** (`AiAuditAdvisor` wraps every
LLM call for cost/latency logging), **Facade** (`RecommendationOrchestrationService`).

Recommendation lifecycle (enforced by the aggregate itself, not callers):
`DRAFT → PENDING_APPROVAL → (APPROVED | REJECTED)` ; `APPROVED → APPLYING → (APPLIED | FAILED)`.

## Tech stack

| Concern | Choice |
|---|---|
| Runtime | Java 21, Spring Boot 4.1.0 (Spring Framework 7) |
| AI | Spring AI 2.0, `spring-ai-starter-model-openai` |
| Persistence | Spring Data JPA (Hibernate 7), PostgreSQL |
| Migrations | Flyway |
| Security | Spring Security 7, stateless JWT, roles `DB_VIEWER` / `DB_ADMIN` |
| Scheduling | `@Scheduled` (metrics polling + retention cleanup) |
| Observability | Actuator + Micrometer/Prometheus, custom AI cost/latency audit |

## Prerequisites

- **JDK 21**
- **PostgreSQL** with `pg_stat_statements` loaded via `shared_preload_libraries` (a config change
  that requires a server restart — `CREATE EXTENSION pg_stat_statements` alone is not enough):
  ```sql
  -- postgresql.conf: shared_preload_libraries = 'pg_stat_statements'
  -- then restart Postgres, then per database you want monitored:
  CREATE EXTENSION IF NOT EXISTS pg_stat_statements;
  ```
- A database for the app's own storage (separate from any database you want to *monitor*).

## Configuration

`application.yaml` has **no defaults** for security-sensitive values — the app fails fast on boot
if they're unset, so a forgotten override can never silently fall back to a known key:

| Env var | Purpose |
|---|---|
| `DB_ADMIN_DATASOURCE_URL` / `_USERNAME` / `_PASSWORD` | The app's own Postgres storage |
| `JWT_SECRET` | Base64, ≥32 bytes (`openssl rand -base64 32`) |
| `CREDENTIAL_ENCRYPTION_KEY` | Base64 32-byte AES key — encrypts monitored-database passwords at rest |
| `OPENAI_API_KEY` | Only needed when a recommendation is actually drafted |

**Local development shortcut**: run with `SPRING_PROFILES_ACTIVE=dev` and none of the above are
required — `application-dev.yaml` supplies working local defaults and seeds an admin user
(`admin@dev.local` / `ChangeMe123!`). Never used in prod; the base config still has no defaults.

## Running

```bash
export SPRING_PROFILES_ACTIVE=dev
./mvnw spring-boot:run
```

## Testing

```bash
export SPRING_PROFILES_ACTIVE=dev
./mvnw test
```
Domain unit tests (state machine transitions, SQL validation, risk assessment) need no database.
The full-context test and any `@SpringBootTest` need a real Postgres reachable via the `dev`
profile's defaults (or your own env vars).

## API reference

Base URL: `http://localhost:8080`. Every response uses the same envelope:
```json
{"success": true, "data": { }, "error": null, "timestamp": "2026-01-01T00:00:00Z"}
```
On failure, `data` is `null` and `error` is `{"code", "message", "details"}`.

### Auth

**`POST /api/v1/auth/login`** — public
```bash
curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@dev.local","password":"ChangeMe123!"}'
```
```json
{"success":true,"data":{"accessToken":"eyJhbGciOiJIUzI1NiJ9...","tokenType":"Bearer"},"timestamp":"..."}
```
Every endpoint below requires `-H "Authorization: Bearer $TOKEN"`. `DB_ADMIN`-only endpoints are
marked; everything else just needs a valid token (`DB_VIEWER` or `DB_ADMIN`).

### Monitored databases

**`POST /api/v1/monitored-databases`** — `DB_ADMIN`. Registers a Postgres target to monitor.
```bash
curl -s -X POST http://localhost:8080/api/v1/monitored-databases \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"name":"orders-db","jdbcUrl":"jdbc:postgresql://localhost:5432/orders","username":"app","password":"secret"}'
```
The password is encrypted at rest (AES-GCM) and never returned in responses.

**`GET /api/v1/monitored-databases`**
```bash
curl -s http://localhost:8080/api/v1/monitored-databases -H "Authorization: Bearer $TOKEN"
```

**`POST /api/v1/monitored-databases/{id}/disable`** / **`/enable`** — `DB_ADMIN`. Disabling stops
the scheduler from polling that target on its next cycle.
```bash
curl -s -X POST "http://localhost:8080/api/v1/monitored-databases/$DB_ID/disable" -H "Authorization: Bearer $TOKEN"
```

### Metrics

**`GET /api/v1/metrics/{databaseId}?limit=50`** — recent instance-level snapshots (connections,
cache hit ratio, lock waits). `limit` capped at 200.
```bash
curl -s "http://localhost:8080/api/v1/metrics/$DB_ID?limit=20" -H "Authorization: Bearer $TOKEN"
```

### Slow queries

**`GET /api/v1/slow-queries?databaseId={id}&limit=50`**
```bash
curl -s "http://localhost:8080/api/v1/slow-queries?databaseId=$DB_ID&limit=20" -H "Authorization: Bearer $TOKEN"
```

### Recommendations

**`GET /api/v1/recommendations?status=PENDING_APPROVAL&limit=50`** — `status` is one of `DRAFT`,
`PENDING_APPROVAL`, `APPROVED`, `REJECTED`, `APPLYING`, `APPLIED`, `FAILED`.
```bash
curl -s "http://localhost:8080/api/v1/recommendations?status=PENDING_APPROVAL" -H "Authorization: Bearer $TOKEN"
```

**`GET /api/v1/recommendations/{id}`**
```bash
curl -s "http://localhost:8080/api/v1/recommendations/$REC_ID" -H "Authorization: Bearer $TOKEN"
```

**`POST /api/v1/recommendations/{id}/approve`** — `DB_ADMIN`, body optional
```bash
curl -s -X POST "http://localhost:8080/api/v1/recommendations/$REC_ID/approve" \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"comment":"looks safe"}'
```

**`POST /api/v1/recommendations/{id}/reject`** — `DB_ADMIN`, body optional
```bash
curl -s -X POST "http://localhost:8080/api/v1/recommendations/$REC_ID/reject" \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"reason":"too risky right now"}'
```

**`POST /api/v1/recommendations/{id}/apply`** — `DB_ADMIN`. Only works from `APPROVED`; actually
runs the vetted DDL (`CREATE INDEX CONCURRENTLY` / `ALTER SYSTEM SET`) against the target database.
```bash
curl -s -X POST "http://localhost:8080/api/v1/recommendations/$REC_ID/apply" -H "Authorization: Bearer $TOKEN"
```

### Audit log

**`GET /api/v1/audit-log?entityType=PerformanceRecommendation&entityId={id}&limit=50`** — omit
`entityType`/`entityId` for the global recent feed.
```bash
curl -s "http://localhost:8080/api/v1/audit-log?limit=20" -H "Authorization: Bearer $TOKEN"
```

### Ops endpoints (not under `/api/v1`, no auth)

`GET /actuator/health`, `GET /actuator/prometheus`, `GET /actuator/metrics`, `GET /actuator/info`

## Sample slow queries to test with

`pg_sleep()` alone is a poor test case — it has no table or column for the AI to index, so it
correctly comes back as "no fix applies". These build a large table with genuinely slow, indexable
queries instead. Run against **a database you've registered via `POST /monitored-databases`**, not
necessarily the app's own storage database.

```sql
CREATE TABLE demo_orders (
    id BIGINT PRIMARY KEY,
    customer_email TEXT NOT NULL,
    status TEXT NOT NULL,
    amount NUMERIC(10,2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

INSERT INTO demo_orders (id, customer_email, status, amount, created_at)
SELECT g, 'customer' || (g % 500000) || '@example.com',
       (ARRAY['PENDING','PAID','SHIPPED','CANCELLED'])[1 + (g % 4)],
       (random() * 1000)::numeric(10,2),
       now() - (random() * interval '365 days')
FROM generate_series(1, 15000000) AS g;
-- 15M rows, no index on status, amount, or customer_email
```

**SEVERE (~2.5-3s, verified — auto-triggers an AI recommendation)**, a real candidate for
`CREATE INDEX CONCURRENTLY ... ON demo_orders (status, amount)`:
```sql
SELECT * FROM demo_orders WHERE status = 'PAID' AND amount BETWEEN 100 AND 900;
```
Run it 3-4 times so `pg_stat_statements`' mean settles comfortably above the SEVERE threshold
(5× `slow-query-threshold-ms`, default 2500ms) — a single run can land just under it.

**MODERATE (~900ms, verified — visible via the API but doesn't auto-trigger AI drafting; only
SEVERE does, to bound AI spend)**. A good edge case: a leading-wildcard `LIKE` can't use a plain
B-tree index, so the right answer here is *not* a naive `CREATE INDEX`:
```sql
SELECT * FROM demo_orders WHERE customer_email LIKE '%customer123456%';
```

After running the SEVERE query, wait for the next scheduler poll (`app.monitoring.poll-interval-ms`,
default 60s) and check `GET /api/v1/recommendations?status=PENDING_APPROVAL`.

**Cleanup** (15M rows is ~1-2GB on disk): `DROP TABLE demo_orders;`

**If `pg_stat_statements` already has unrelated history** for the database you registered (old
benchmarks, accidental Cartesian-product joins, etc.), the first poll will sweep all of it into
recommendations at once. Reset for a clean baseline before testing:
```sql
SELECT pg_stat_statements_reset();
```

## Known limitations

- No `DELETE` for a monitored database, only `disable`/`enable` — a bad registration stays visible
  but stops being polled.
- Each captured slow-query event is analyzed independently, so the same underlying issue captured
  across multiple polls can produce multiple overlapping recommendations (e.g. two different index
  proposals for the same query). This is by design — the human approval step is exactly where you
  pick the right one and reject the rest — but expect duplicates when testing.
- `pg_stat_statements` is cumulative per database, not a rolling window — registering a database
  with a long history surfaces all of its historically-slow queries on the first poll, not just new
  activity going forward.
