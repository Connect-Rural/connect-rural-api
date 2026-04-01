-- liquibase formatted sql


-- ============================================================
-- changeset israel-CR:003-001
-- file_objects
-- ============================================================
CREATE TABLE IF NOT EXISTS connect_rural.file_objects (
    file_key        UUID PRIMARY KEY DEFAULT public.uuid_generate_v4(),
    bucket          VARCHAR(100) NOT NULL,
    object_key      VARCHAR(500) NOT NULL,
    original_name   VARCHAR(255) NOT NULL,
    content_type    VARCHAR(150) NULL,
    size            BIGINT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'READY',
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_file_objects_status ON connect_rural.file_objects(status);
