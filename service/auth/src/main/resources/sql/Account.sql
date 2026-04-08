-- Root
CREATE TABLE accounts (
    id BIGINT NOT NULL,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(320) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    inserted_at TIMESTAMPZ WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPZ WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMPZ WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_accounts PRIMARY KEY (id),
    CONSTRAINT unq_accounts_username UNIQUE (username),
    CONSTRAINT unq_accounts_email UNIQUE (email),
    CONSTRAINT unq_accounts_phone_number UNIQUE (phone_number),
)