-- Root
CREATE TABLE users (
    id BIGINT NOT NULL DEFAULT GENERATE_TSID(), -- PK
    email VARCHAR(320) NOT NULL, -- UNQ
    last_login TIMESTAMP WITH TIME ZONE,
    is_account_locked BOOLEAN DEFAULT FALSE,
    is_account_available BOOLEAN DEFAULT FALSE,
    inserted_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT unq_users_email UNIQUE (email)
);

-- [Root:1] Credential
CREATE TABLE user_credentials (
    id BIGINT NOT NULL DEFAULT GENERATE_TSID(), -- PK
    user_id BIGINT NOT NULL, -- UNQ, FK(users.id)
    password_hash TEXT NOT NULL,
    last_password_change TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_user_credentials PRIMARY KEY (id),
    CONSTRAINT unq_user_credentials_user_id UNIQUE (user_id),
    CONSTRAINT fk_user_credentials_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- [Root:N] Oauth (HardDelete)
CREATE TABLE user_oauth_identities (
    id BIGINT NOT NULL DEFAULT GENERATE_TSID(), -- PK
    user_id BIGINT NOT NULL, -- FK(users.id)
    provider VARCHAR(30) NOT NULL,
    provider_subject VARCHAR(200) NOT NULL,
    email VARCHAR(320),
    is_email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    linked_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    inserted_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_user_oauth_identities PRIMARY KEY (id),
    CONSTRAINT fk_user_oauth_identities_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT unq_user_oauth_identities_user_id_provider UNIQUE (user_id, provider),
    CONSTRAINT unq_user_oauth_identities_provider_provider_subject UNIQUE (provider, provider_subject)
);

-- [Root:1] Profile
CREATE TABLE user_profiles (
    id BIGINT NOT NULL DEFAULT GENERATE_TSID(), -- PK
    user_id BIGINT NOT NULL, -- FK(users.id)
    username VARCHAR(100) NOT NULL,
    locale VARCHAR(20),
    time_zone VARCHAR(50),
    gender VARCHAR(20),
    phone VARCHAR(20),
    birth_date DATE,
    CONSTRAINT pk_user_profiles PRIMARY KEY (id),
    CONSTRAINT unq_user_profiles_user_id UNIQUE (user_id),
    CONSTRAINT unq_user_profiles_username UNIQUE (username),
    CONSTRAINT fk_user_profiles_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);