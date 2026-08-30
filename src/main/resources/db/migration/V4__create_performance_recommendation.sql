CREATE TABLE performance_recommendation (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    database_id         UUID NOT NULL REFERENCES monitored_database(id),
    slow_query_event_id UUID REFERENCES slow_query_event(id),
    type                VARCHAR(30)  NOT NULL,
    status              VARCHAR(30)  NOT NULL DEFAULT 'DRAFT',
    risk_level          VARCHAR(20)  NOT NULL,
    title               VARCHAR(255) NOT NULL,
    explanation         TEXT         NOT NULL,
    proposed_sql        TEXT         NOT NULL,
    target_object       VARCHAR(255),
    failure_reason      TEXT,
    applied_at          TIMESTAMPTZ,
    version             BIGINT       NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_performance_recommendation_database_status ON performance_recommendation(database_id, status);
CREATE INDEX idx_performance_recommendation_status ON performance_recommendation(status);
