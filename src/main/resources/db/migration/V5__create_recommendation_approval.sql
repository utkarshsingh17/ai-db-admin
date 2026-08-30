CREATE TABLE recommendation_approval (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recommendation_id   UUID NOT NULL REFERENCES performance_recommendation(id),
    decision            VARCHAR(20) NOT NULL,
    admin_user_id       UUID NOT NULL REFERENCES admin_user(id),
    comment             TEXT,
    decided_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_recommendation_approval_recommendation ON recommendation_approval(recommendation_id);
