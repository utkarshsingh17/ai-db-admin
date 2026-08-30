CREATE TABLE monitored_database (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                VARCHAR(255) NOT NULL UNIQUE,
    engine              VARCHAR(50)  NOT NULL DEFAULT 'POSTGRESQL',
    jdbc_url            VARCHAR(500) NOT NULL,
    username            VARCHAR(255) NOT NULL,
    encrypted_password  VARCHAR(1000) NOT NULL,
    enabled             BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_monitored_database_enabled ON monitored_database(enabled);
