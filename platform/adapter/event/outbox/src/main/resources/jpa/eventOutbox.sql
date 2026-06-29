CREATE TABLE event_outbox (
    event_id BIGINT PRIMARY KEY,
    trace_id VARCHAR(64),
    causation_id VARCHAR(64),
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    issuer VARCHAR(100),
    producer VARCHAR(100),
    schema_version VARCHAR(20) NOT NULL,
    partition_key BIGINT NOT NULL,
    event_scope VARCHAR(20) NOT NULL,
    event_type VARCHAR(200) NOT NULL,
    payload BYTEA NOT NULL,
    payload_json TEXT NOT NULL,
    metadata_json TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    attempt_count INT NOT NULL DEFAULT 0,
    last_attempted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_event_outbox_status_occurred_at ON event_outbox(status, occurred_at);