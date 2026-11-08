-- liquibase formatted sql

-- ============================================================
-- changeset israel-CR:002-01
-- cooperations
-- Incluye campos de recargo (late_fee) y tipo de asignación
-- closed_at indica cuándo se cerró la cooperación
-- ============================================================
CREATE TABLE IF NOT EXISTS connect_rural.cooperations (
    cooperation_key     UUID PRIMARY KEY DEFAULT public.uuid_generate_v4(),
    community_key       UUID NOT NULL,
    name                VARCHAR(200) NOT NULL,
    description         TEXT NULL,
    base_amount         DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    start_date          DATE NOT NULL DEFAULT CURRENT_DATE,
    due_date            DATE NULL,
    allow_late_fee      BOOLEAN NOT NULL DEFAULT FALSE,
    late_fee_amount     DECIMAL(12,2) NULL,
    late_fee_period     VARCHAR(50) NULL,       -- DAILY | WEEKLY | MONTHLY
    assignment_type     VARCHAR(50) NOT NULL DEFAULT 'ALL', -- ALL | MANUAL
    status              VARCHAR(50) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE | CLOSED | CANCELLED
    closed_at           TIMESTAMP NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_cooperation_community FOREIGN KEY (community_key)
    REFERENCES connect_rural.communities(community_key)
    ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT chk_cooperation_status
    CHECK (status IN ('ACTIVE', 'CLOSED', 'CANCELLED')),

    CONSTRAINT chk_cooperation_assignment_type
    CHECK (assignment_type IN ('ALL', 'MANUAL')),

    CONSTRAINT chk_late_fee_period
    CHECK (late_fee_period IN ('DAILY', 'WEEKLY', 'MONTHLY') OR late_fee_period IS NULL)
    );

CREATE INDEX IF NOT EXISTS idx_cooperation_community ON connect_rural.cooperations(community_key);
CREATE INDEX IF NOT EXISTS idx_cooperation_status    ON connect_rural.cooperations(status);



-- ============================================================
-- changeset israel-CR:002-02
-- financial_obligations
-- Modela la deuda de un residente originada por cooperación,
-- multa o trabajo comunitario (origin_type + origin_id polimórfico)
-- ============================================================
CREATE TABLE IF NOT EXISTS connect_rural.financial_obligations (
    obligation_key      UUID PRIMARY KEY DEFAULT public.uuid_generate_v4(),
    resident_key        UUID NOT NULL,
    community_key       UUID NOT NULL,

    -- FK polimórfica: apunta a cooperations, fines o community_works
    origin_type         VARCHAR(50) NOT NULL,   -- COOPERATION | FINE | COMMUNITY_WORK
    origin_id           UUID NOT NULL,

    amount_due          DECIMAL(12,2) NOT NULL,
    late_fee_applied    DECIMAL(12,2) NOT NULL DEFAULT 0.00,

    status              VARCHAR(50) NOT NULL DEFAULT 'PENDING', -- PENDING | PARTIAL | PAID
    due_date            DATE NULL,

    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_fobligation_resident FOREIGN KEY (resident_key)
    REFERENCES connect_rural.residents(resident_key)
    ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT fk_fobligation_community FOREIGN KEY (community_key)
    REFERENCES connect_rural.communities(community_key)
    ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT chk_fobligation_origin_type
    CHECK (origin_type IN ('COOPERATION', 'FINE', 'COMMUNITY_WORK')),

    CONSTRAINT chk_fobligation_status
    CHECK (status IN ('PENDING', 'PARTIAL', 'PAID'))
    );

CREATE INDEX IF NOT EXISTS idx_fobligation_resident  ON connect_rural.financial_obligations(resident_key);
CREATE INDEX IF NOT EXISTS idx_fobligation_community ON connect_rural.financial_obligations(community_key);
CREATE INDEX IF NOT EXISTS idx_fobligation_origin    ON connect_rural.financial_obligations(origin_type, origin_id);
CREATE INDEX IF NOT EXISTS idx_fobligation_status    ON connect_rural.financial_obligations(status);


-- ============================================================
-- changeset israel-CR:002-03
-- resident_payments
-- Registro del dinero recibido por parte de un residente.
-- No referencia directamente la obligación — eso lo hace
-- payment_allocations, permitiendo distribuir un pago entre
-- múltiples obligaciones.
-- ============================================================
CREATE TABLE IF NOT EXISTS connect_rural.resident_payments (
    payment_key     UUID PRIMARY KEY DEFAULT public.uuid_generate_v4(),
    resident_key    UUID NOT NULL,

    amount          DECIMAL(12,2) NOT NULL,
    method          VARCHAR(50) NULL,       -- CASH | TRANSFER | CHECK
    reference       VARCHAR(200) NULL,      -- folio, número de transferencia, etc.

    paid_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_rpayment_resident FOREIGN KEY (resident_key)
    REFERENCES connect_rural.residents(resident_key)
    ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT chk_rpayment_method
    CHECK (method IN ('CASH', 'TRANSFER', 'CHECK') OR method IS NULL)
    );

CREATE INDEX IF NOT EXISTS idx_rpayment_resident ON connect_rural.resident_payments(resident_key);
CREATE INDEX IF NOT EXISTS idx_rpayment_paid_at  ON connect_rural.resident_payments(paid_at);


-- ============================================================
-- changeset israel-CR:002-04
-- payment_allocations
-- Distribuye un pago entre una o más obligaciones financieras.
-- Un pago puede cubrir múltiples deudas; una deuda puede
-- recibir múltiples pagos parciales.
-- ============================================================
CREATE TABLE IF NOT EXISTS connect_rural.payment_allocations (
    allocation_key  UUID PRIMARY KEY DEFAULT public.uuid_generate_v4(),
    payment_key     UUID NOT NULL,
    obligation_key  UUID NOT NULL,

    amount_applied  DECIMAL(12,2) NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_allocation_payment FOREIGN KEY (payment_key)
    REFERENCES connect_rural.resident_payments(payment_key)
    ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT fk_allocation_obligation FOREIGN KEY (obligation_key)
    REFERENCES connect_rural.financial_obligations(obligation_key)
    ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT uq_allocation_payment_obligation
    UNIQUE (payment_key, obligation_key)
    );

CREATE INDEX IF NOT EXISTS idx_allocation_payment    ON connect_rural.payment_allocations(payment_key);
CREATE INDEX IF NOT EXISTS idx_allocation_obligation ON connect_rural.payment_allocations(obligation_key);
