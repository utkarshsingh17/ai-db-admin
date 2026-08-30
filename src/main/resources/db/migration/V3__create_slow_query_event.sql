CREATE TABLE slow_query_event (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    database_id         UUID NOT NULL REFERENCES monitored_database(id),
    query_fingerprint   VARCHAR(64)  NOT NULL,
    normalized_query    TEXT         NOT NULL,
    calls               BIGINT       NOT NULL,
    mean_exec_time_ms    DOUBLE PRECISION NOT NULL,
    total_exec_time_ms   DOUBLE PRECISION NOT NULL,
    rows_returned       BIGINT,
    captured_at         TIMESTAMPTZ  NOT NULL,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_slow_query_event_database_captured ON slow_query_event(database_id, captured_at DESC);
CREATE INDEX idx_slow_query_event_fingerprint ON slow_query_event(query_fingerprint);
