CREATE TABLE login_change_request (
    id UUID PRIMARY KEY,
    requester_user_id UUID NOT NULL REFERENCES app_user (id),
    change_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    proposed_email VARCHAR(255) NULL,
    reviewed_by_user_id UUID NULL REFERENCES app_user (id),
    reviewed_at TIMESTAMPTZ NULL,
    completed_at TIMESTAMPTZ NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    rejection_reason VARCHAR(500) NULL
);

CREATE INDEX idx_login_change_status ON login_change_request (status);
CREATE INDEX idx_login_change_requester ON login_change_request (requester_user_id);
