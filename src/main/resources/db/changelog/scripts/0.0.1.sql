-- liquibase formatted sql

-- changeset israel-CR:261125-001
CREATE EXTENSION IF NOT EXISTS "uuid-ossp" SCHEMA public;


CREATE TABLE IF NOT EXISTS communities (
    community_key UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(200) NOT NULL,
    description TEXT NULL,
    logo_url VARCHAR(500) NULL,
    address VARCHAR(300) NULL,
    state VARCHAR(100) NULL,
    municipality VARCHAR(100) NULL,
    postal_code VARCHAR(20) NULL,
    subscription_plan VARCHAR(100) NULL,
    completed_configuration BOOLEAN DEFAULT FALSE,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
