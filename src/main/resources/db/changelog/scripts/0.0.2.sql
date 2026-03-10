-- liquibase formatted sql

-- changeset israel-CR:050326-001
ALTER TABLE communities ADD COLUMN IF NOT EXISTS whatsapp_app_key UUID NULL;
