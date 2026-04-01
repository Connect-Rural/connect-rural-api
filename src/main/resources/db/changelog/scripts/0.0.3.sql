-- liquibase formatted sql

-- ============================================================
-- changeset israel-CR:003-001
-- expense_categories
-- ============================================================
CREATE TABLE IF NOT EXISTS connect_rural.expense_categories (
    category_key    UUID PRIMARY KEY DEFAULT public.uuid_generate_v4(),
    community_key   UUID NOT NULL,
    name            VARCHAR(150) NOT NULL,
    description     TEXT NULL,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_expcat_community FOREIGN KEY (community_key)
    REFERENCES connect_rural.communities(community_key)
    ON DELETE CASCADE ON UPDATE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_expcat_community ON connect_rural.expense_categories(community_key);


-- ============================================================
-- changeset israel-CR:003-002
-- community_expenses
-- ============================================================
CREATE TABLE IF NOT EXISTS connect_rural.community_expenses (
    expense_key     UUID PRIMARY KEY DEFAULT public.uuid_generate_v4(),
    community_key   UUID NOT NULL,
    category_key    UUID NULL,
    description     TEXT NOT NULL,
    amount          DECIMAL(12,2) NOT NULL,
    expense_date    DATE NOT NULL DEFAULT CURRENT_DATE,
    created_by      UUID NULL,  -- FK a usuarios cuando exista el módulo
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_expense_community FOREIGN KEY (community_key)
    REFERENCES connect_rural.communities(community_key)
    ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT fk_expense_category FOREIGN KEY (category_key)
    REFERENCES connect_rural.expense_categories(category_key)
    ON DELETE SET NULL ON UPDATE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_expense_community ON connect_rural.community_expenses(community_key);
CREATE INDEX IF NOT EXISTS idx_expense_date      ON connect_rural.community_expenses(expense_date);