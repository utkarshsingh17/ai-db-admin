CREATE TABLE sensitive_column (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    database_id  UUID NOT NULL REFERENCES monitored_database(id),
    table_name   VARCHAR(255) NOT NULL,
    column_name  VARCHAR(255) NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (database_id, table_name, column_name)
);

CREATE INDEX idx_sensitive_column_database ON sensitive_column(database_id);
