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

-- changeset israel-CR:031225-001
CREATE TABLE IF NOT EXISTS residents(
    resident_key UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    community_key UUID NOT NULL,
    first_name VARCHAR(200) NOT NULL,
    last_name VARCHAR(200) NULL,
    birth_date DATE NOT NULL,
    phone_number VARCHAR(20) NULL,
    email VARCHAR(255) NULL,
    address TEXT NULL,
    address_reference TEXT NULL,
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_resident_community FOREIGN KEY(community_key)
     REFERENCES communities(community_key) ON DELETE CASCADE ON UPDATE CASCADE

);

CREATE INDEX IF NOT EXISTS idx_resident_community_key ON residents(community_key);
