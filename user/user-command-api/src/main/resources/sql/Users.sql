-- 사용자
CREATE TABLE users (
    id BIGINT NOT NULL DEFAULT GENERATE_TSID(), -- PK
    username VARCHAR(70) NOT NULL, -- UNIQUE SOFT_DELETE
    personal_name VARCHAR(20) NOT NULL,
    organization_id BIGINT NOT NULL,
    contact VARCHAR(20),
    email VARCHAR(60) NOT NULL, -- UNIQUE
    last_login TIMESTAMP WITH TIME ZONE,
    failed_login_attempts INT DEFAULT 0,
    is_account_locked BOOLEAN DEFAULT FALSE,
    is_account_available BOOLEAN DEFAULT FALSE,
    inserted TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT unq_users_username UNIQUE (username),
    CONSTRAINT unq_users_email UNIQUE (email)
);