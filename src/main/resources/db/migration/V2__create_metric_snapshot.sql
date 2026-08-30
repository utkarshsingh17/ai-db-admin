CREATE TABLE metric_snapshot (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    database_id         UUID NOT NULL REFERENCES monitored_database(id),
    active_connections  INT,
    max_connections     INT,
    cache_hit_ratio     DOUBLE PRECISION,
    lock_wait_count     INT,
    captured_at         TIMESTAMPTZ NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_metric_snapshot_database_captured ON metric_snapshot(database_id, captured_at DESC);
