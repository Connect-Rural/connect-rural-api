-- liquibase formatted sql

-- ============================================================
-- changeset israel-CR:007-001
-- cooperation_periods
-- ============================================================
CREATE TABLE IF NOT EXISTS connect_rural.cooperation_periods (
    cooperation_period_key        UUID PRIMARY KEY DEFAULT public.uuid_generate_v4(),
    cooperation_key               UUID NOT NULL,
    period_number                 INT NOT NULL,
    name                          VARCHAR(150) NOT NULL,
    start_date                    DATE NOT NULL,
    end_date                      DATE NOT NULL,
    due_date                      DATE NOT NULL,
    amount                        DECIMAL(12,2) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE | CLOSED | CANCELLED
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_cooperation_period_cooperation FOREIGN KEY (cooperation_key)
    REFERENCES connect_rural.cooperations(cooperation_key)
    ON DELETE CASCADE ON UPDATE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_cooperation_period_cooperation ON connect_rural.cooperation_periods(cooperation_key);
CREATE INDEX IF NOT EXISTS idx_cooperation_period_status      ON connect_rural.cooperation_periods(status);

-- ============================================================
-- changeset israel-CR:007-002
-- cooperation_assignments
-- ============================================================

CREATE TABLE connect_rural.cooperation_assignments (
    assignment_key UUID PRIMARY KEY,
    cooperation_key UUID NOT NULL,
    resident_key UUID NOT NULL,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    removed_at TIMESTAMP,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_assignment_cooperation
        FOREIGN KEY (cooperation_key)
            REFERENCES connect_rural.cooperations(cooperation_key),

    CONSTRAINT fk_assignment_resident
        FOREIGN KEY (resident_key)
            REFERENCES connect_rural.residents(resident_key),

    CONSTRAINT uq_cooperation_resident
        UNIQUE (cooperation_key, resident_key)
);

CREATE INDEX IF NOT EXISTS idx_assignment_cooperation ON connect_rural.cooperation_assignments(cooperation_key);
CREATE INDEX IF NOT EXISTS idx_assignment_resident ON connect_rural.cooperation_assignments(resident_key);
CREATE INDEX IF NOT EXISTS idx_assignment_cooperation_resident    ON connect_rural.cooperation_assignments(cooperation_key, resident_key);


-- changeset israel-CR:007-003
-- cooperation_assigmnets, poblate from financial obligations
-- ============================================================

INSERT INTO connect_rural.cooperation_assignments (
    assignment_key,
    cooperation_key,
    resident_key,
    status,
    assigned_at,
    created_at,
    updated_at
)
SELECT
    gen_random_uuid(),
    c.cooperation_key,
    fo.resident_key,
    'ACTIVE',
    MIN(fo.created_at),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM connect_rural.financial_obligations fo
         JOIN connect_rural.cooperations c
              ON c.cooperation_key = fo.origin_id::uuid
JOIN connect_rural.residents r
ON r.resident_key = fo.resident_key
WHERE fo.origin_type = 'COOPERATION'
GROUP BY
    c.cooperation_key,
    fo.resident_key
ON CONFLICT (cooperation_key, resident_key)
    DO NOTHING;


-- changeset israel-CR:007-004
-- cooperation_periods, poblate from financial obligations
-- ============================================================
INSERT INTO connect_rural.cooperation_periods (
    cooperation_period_key,
    cooperation_key,
    period_number,
    name,
    start_date,
    end_date,
    due_date,
    amount,
    status,
    created_at,
    updated_at
)
SELECT
    gen_random_uuid(),
    c.cooperation_key,

    ROW_NUMBER() OVER (
        PARTITION BY c.cooperation_key
        ORDER BY fo.period_ref
    )::INTEGER,

    TO_CHAR(
            fo.period_ref,
            'DD/MM/YYYY'
    ),

    fo.period_ref,
    fo.period_ref,
    MAX(fo.due_date),
    MAX(fo.amount_due),

    CASE
        WHEN MAX(fo.due_date) < CURRENT_DATE
            THEN 'OPEN'
        ELSE 'PENDING'
        END,

    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP

FROM connect_rural.financial_obligations fo

         JOIN connect_rural.cooperations c
              ON c.cooperation_key = fo.origin_id

WHERE fo.origin_type = 'COOPERATION'
  AND fo.period_ref IS NOT NULL

GROUP BY
    c.cooperation_key,
    fo.period_ref;