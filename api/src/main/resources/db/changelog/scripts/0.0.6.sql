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


-- ============================================================
-- changeset israel-CR:100526-001
-- add periodicty and end_date columns to cooperations table
-- ============================================================
ALTER TABLE connect_rural.cooperations
    ADD COLUMN periodicity VARCHAR(20) NOT NULL DEFAULT 'ONE_TIME'
    CHECK (periodicity IN ('ONE_TIME','MONTHLY','QUARTERLY','ANNUAL')),
    ADD COLUMN end_date DATE NULL;




-- ============================================================
-- changeset israel-CR:100526-002
-- add period_ref column to financial_obligations table
-- ============================================================
ALTER TABLE connect_rural.financial_obligations
    ADD COLUMN period_ref DATE NULL;



-- changeset israel-CR:100526-003
-- add period ref date to financial obligations from cooperations

UPDATE connect_rural.financial_obligations fo
SET period_ref = c.start_date
FROM connect_rural.cooperations c
WHERE fo.origin_type = 'COOPERATION'
  AND fo.origin_id = c.cooperation_key
  AND c.periodicity = 'ONE_TIME';
