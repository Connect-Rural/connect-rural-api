-- liquibase formatted sql

-- ============================================================
-- changeset israel-CR:010426-001
-- fix chk_cooperation_assignment_type: replace MANUAL with INDIVIDUAL and ALL_EXCEPT
-- ============================================================
ALTER TABLE connect_rural.cooperations
    DROP CONSTRAINT chk_cooperation_assignment_type;

ALTER TABLE connect_rural.cooperations
    ADD CONSTRAINT chk_cooperation_assignment_type
    CHECK (assignment_type IN ('ALL', 'INDIVIDUAL', 'ALL_EXCEPT'));
