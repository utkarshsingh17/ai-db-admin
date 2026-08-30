-- Technical/cost audit of every raw LLM call, distinct from audit_log_entry (business/compliance
-- log of recommendation lifecycle actions). Written by the AiAuditAdvisor decorator on every
-- ChatClient invocation.
CREATE TABLE ai_audit_log (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    request_id          VARCHAR(64) NOT NULL,
    operation           VARCHAR(100) NOT NULL,
    model               VARCHAR(100) NOT NULL,
    input_tokens        INT NOT NULL DEFAULT 0,
    output_tokens       INT NOT NULL DEFAULT 0,
    estimated_cost_usd  DOUBLE PRECISION NOT NULL DEFAULT 0,
    latency_ms          BIGINT NOT NULL,
    success             BOOLEAN NOT NULL,
    error_message       TEXT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_ai_audit_log_created_at ON ai_audit_log(created_at DESC);
