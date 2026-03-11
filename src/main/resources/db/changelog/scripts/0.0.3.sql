-- liquibase formatted sql

-- changeset israel-CR:100326-001
ALTER TABLE connect_rural.cooperations ADD COLUMN IF NOT EXISTS closed_at TIMESTAMP NULL;
