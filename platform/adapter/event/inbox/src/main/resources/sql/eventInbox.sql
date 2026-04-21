CREATE TABLE event_inbox (
    event_id BIGINT PRIMARY KEY,
    trace_id VARCHAR(255) NOT NULL,
    causation_id VARCHAR(255),
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    issuer VARCHAR(255) NOT NULL,
    producer VARCHAR(255) NOT NULL,
    schema_version VARCHAR(255) NOT NULL,
    partition_key BIGINT NOT NULL,
    event_scope VARCHAR(50) NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    payload BYTEA NOT NULL,
    payload_json TEXT NOT NULL,
    metadata_json TEXT NOT NULL,
    status VARCHAR(50) NOT NULL,
    received_at TIMESTAMP WITH TIME ZONE NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE,
    attempt_count INT NOT NULL,
    last_attempted_at TIMESTAMP WITH TIME ZONE,
    error_message TEXT
);

--CREATE INDEX idx_event_inbox_status_occurred_at ON event_inbox(status, occurred_at);