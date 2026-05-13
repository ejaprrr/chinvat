-- H2 schema for local development (without PostgreSQL ENUM types)
-- This is auto-loaded by spring.sql.init configuration to avoid NAMED_ENUM issues

-- Table: auth_session
-- Maps ENUM types as VARCHAR for H2 compatibility
CREATE TABLE IF NOT EXISTS auth_session (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    user_id UUID NOT NULL,
    session_token_hash VARCHAR(255) NOT NULL UNIQUE,
    session_token_kind VARCHAR(20) NOT NULL CHECK (session_token_kind IN ('ACCESS', 'REFRESH')),
    issued_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP()
);

-- Table: users (account management)
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    email VARCHAR(255) NOT NULL UNIQUE,
    account_status VARCHAR(20) NOT NULL CHECK (account_status IN ('PENDING', 'ACTIVE', 'SUSPENDED', 'DELETED')),
    account_role VARCHAR(20) NOT NULL CHECK (account_role IN ('ADMIN', 'USER', 'GUEST')),
    preferred_lang VARCHAR(10),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP()
);

-- Table: user_certificates
CREATE TABLE IF NOT EXISTS user_certificates (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    user_id UUID NOT NULL,
    certificate_data BLOB NOT NULL,
    issuer_name VARCHAR(500),
    subject_name VARCHAR(500),
    serial_number VARCHAR(255),
    not_before TIMESTAMP,
    not_after TIMESTAMP,
    fingerprint_sha256 VARCHAR(255),
    is_primary BOOLEAN DEFAULT FALSE,
    is_revoked BOOLEAN DEFAULT FALSE,
    revoked_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Table: eidas_auth_sessions (eIDAS state management)
CREATE TABLE IF NOT EXISTS eidas_auth_sessions (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    state_key VARCHAR(255) NOT NULL UNIQUE,
    auth_level VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    expires_at TIMESTAMP NOT NULL
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_auth_session_user_id ON auth_session(user_id);
CREATE INDEX IF NOT EXISTS idx_auth_session_token_hash ON auth_session(session_token_hash);
CREATE INDEX IF NOT EXISTS idx_user_certificates_user_id ON user_certificates(user_id);
CREATE INDEX IF NOT EXISTS idx_user_certificates_fingerprint ON user_certificates(fingerprint_sha256);
CREATE INDEX IF NOT EXISTS idx_eidas_state_key ON eidas_auth_sessions(state_key);
