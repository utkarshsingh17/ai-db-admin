-- Append-only audit trail. No UPDATE/DELETE is ever issued against this table by the application
-- (AuditLogRepository exposes only save/find methods) — it is the compliance record of every
-- recommendation and every action taken on it.
CREATE TABLE audit_log_entry (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    actor           VARCHAR(255) NOT NULL,
    action          VARCHAR(50)  NOT NULL,
    entity_type     VARCHAR(50)  NOT NULL,
    entity_id       VARCHAR(64)  NOT NULL,
    payload         TEXT,
    correlation_id  VARCHAR(64),
    occurred_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_log_entity ON audit_log_entry(entity_type, entity_id);
CREATE INDEX idx_audit_log_occurred_at ON audit_log_entry(occurred_at DESC);
