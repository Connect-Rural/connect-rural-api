-- liquibase formatted sql

-- changeset israel-CR:001-01
CREATE EXTENSION IF NOT EXISTS "uuid-ossp" SCHEMA public;
CREATE SCHEMA IF NOT EXISTS connect_rural;


-- ============================================================
-- changeset israel-CR:001-02
-- communities
-- ============================================================
CREATE TABLE IF NOT EXISTS connect_rural.communities (
    community_key           UUID PRIMARY KEY DEFAULT public.uuid_generate_v4(),
    name                    VARCHAR(200) NOT NULL,
    description             TEXT NULL,
    logo_url                VARCHAR(500) NULL,
    address                 VARCHAR(300) NULL,
    state                   VARCHAR(100) NULL,
    municipality            VARCHAR(100) NULL,
    postal_code             VARCHAR(20) NULL,
    subscription_plan       VARCHAR(100) NULL,
    completed_configuration BOOLEAN NOT NULL DEFAULT FALSE,
    active                  BOOLEAN NOT NULL DEFAULT TRUE,
    whatsapp_app_key        UUID NULL,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );


-- ============================================================
-- changeset israel-CR:001-03
-- residents
-- ============================================================
CREATE TABLE IF NOT EXISTS connect_rural.residents (
    resident_key        UUID PRIMARY KEY DEFAULT public.uuid_generate_v4(),
    community_key       UUID NOT NULL,
    first_name          VARCHAR(200) NOT NULL,
    last_name           VARCHAR(200) NULL,
    birth_date          DATE NULL,
    phone_number        VARCHAR(20) NULL,
    email               VARCHAR(255) NULL,
    address             TEXT NULL,
    address_reference   TEXT NULL,
    joined_at           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    active              BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_resident_community FOREIGN KEY (community_key)
    REFERENCES connect_rural.communities(community_key)
    ON DELETE CASCADE ON UPDATE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_resident_community ON connect_rural.residents(community_key);
