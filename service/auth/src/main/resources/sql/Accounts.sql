-- Root
CREATE TABLE accounts (
    id BIGINT NOT NULL, -- PK
    username VARCHAR(50) NOT NULL, -- UNQ
    email VARCHAR(320) NOT NULL, -- UNQ
    phone_number VARCHAR(20) NOT NULL, -- UNQ
    inserted_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    last_fencing_token BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_accounts PRIMARY KEY (id),
    CONSTRAINT unq_accounts_username UNIQUE (username),
    CONSTRAINT unq_accounts_email UNIQUE (email),
    CONSTRAINT unq_accounts_phone_number UNIQUE (phone_number)
);

-- [Root:1]
CREATE TABLE credentials(
    id BIGINT NOT NULL, -- PK
    account_id BIGINT NOT NULL, -- UNQ
    password_hash VARCHAR(255) NOT NULL,
    password_updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    password_expired_at TIMESTAMP WITH TIME ZONE,
    failed_attempts INT NOT NULL DEFAULT 0,
    is_locked BOOLEAN DEFAULT FALSE,
    locked_reason VARCHAR(255),
    locked_at TIMESTAMP WITH TIME ZONE,
    last_login_at TIMESTAMP WITH TIME ZONE,
    last_failed_at TIMESTAMP WITH TIME ZONE,
    mfa_secret VARCHAR(255),
    password_history JSONB,
    CONSTRAINT pk_credentials PRIMARY KEY (id),
    CONSTRAINT unq_credentials_account_id UNIQUE (account_id),
    CONSTRAINT fk_credentials_account_id FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE
);
