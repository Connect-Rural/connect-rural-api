# Plan: Módulo Contable / Presupuesto

## Contexto

Con cooperaciones y multas el sistema ya registra ingresos. Este módulo agrega el lado de los egresos (gastos, compras, servicios) y cierra el ciclo financiero de la comunidad: ingresos vs egresos → balance mensual. Convierte la app de "cobrador" a "administrador financiero".

---

## Conceptos del módulo

| Concepto | Parte | Descripción |
|----------|-------|-------------|
| `income` | A | Ingreso calculado (cooperación pagada, multa pagada) |
| `expense` | A | Egreso registrado manualmente (compra, servicio, mantenimiento) |
| `balance` | A | Vista calculada: ingresos − egresos por período |
| `budget` | B | Presupuesto planificado por categoría para un período |

Los ingresos de cooperaciones y multas se generan automáticamente al marcar como pagado. Los egresos se registran manualmente por el admin.

---

## Modelo de datos

### `expense_categories` — catálogo de categorías de gasto

```sql
CREATE TABLE connect_rural.expense_categories (
    category_key    UUID PRIMARY KEY DEFAULT public.uuid_generate_v4(),
    community_key   UUID NOT NULL,
    name            VARCHAR(100) NOT NULL,   -- Mantenimiento, Servicios, Limpieza, etc.
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_expcat_community FOREIGN KEY (community_key)
        REFERENCES connect_rural.communities(community_key) ON DELETE CASCADE
);
```

### `expenses` — egresos registrados

```sql
CREATE TABLE connect_rural.expenses (
    expense_key     UUID PRIMARY KEY DEFAULT public.uuid_generate_v4(),
    community_key   UUID NOT NULL,
    category_key    UUID NOT NULL,
    description     VARCHAR(255) NOT NULL,
    amount          DECIMAL(12,2) NOT NULL,
    expense_date    DATE NOT NULL,
    registered_by   UUID NULL,              -- user_key del admin que lo registró
    receipt_file    UUID NULL,              -- file_key (MinIO) del comprobante
    notes           TEXT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_expense_community FOREIGN KEY (community_key)
        REFERENCES connect_rural.communities(community_key) ON DELETE CASCADE,
    CONSTRAINT fk_expense_category  FOREIGN KEY (category_key)
        REFERENCES connect_rural.expense_categories(category_key) ON DELETE RESTRICT
);
CREATE INDEX IF NOT EXISTS idx_expense_community_date ON connect_rural.expenses(community_key, expense_date);
```

### `budgets` — presupuesto por categoría y período

```sql
CREATE TABLE connect_rural.budgets (
    budget_key      UUID PRIMARY KEY DEFAULT public.uuid_generate_v4(),
    community_key   UUID NOT NULL,
    category_key    UUID NULL,              -- NULL = presupuesto general
    period_ref      DATE NOT NULL,          -- primer día del mes: 2026-04-01
    planned_amount  DECIMAL(12,2) NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_budget_community FOREIGN KEY (community_key)
        REFERENCES connect_rural.communities(community_key) ON DELETE CASCADE,
    CONSTRAINT uq_budget_community_category_period
        UNIQUE (community_key, category_key, period_ref)
);
```

---

## Vista de balance (calculada, sin tabla propia)

```
BalanceResponse
  ├── period         "Abril 2026"
  ├── periodRef      "2026-04-01"
  ├── income
  │     ├── fromCooperations   9500.00
  │     ├── fromFines           300.00
  │     └── total              9800.00
  ├── expenses
  │     ├── byCategory: [{ name, planned, actual, variance }]
  │     └── total              4200.00
  ├── balance        5600.00
  └── budgetVariance  800.00   (presupuesto total - gasto real)
```

---

## Endpoints

```
# Categorías de gasto
GET    /api/{communityKey}/expense-categories
POST   /api/{communityKey}/expense-categories
DELETE /api/{communityKey}/expense-categories/{categoryKey}

# Egresos
GET    /api/{communityKey}/expenses?month=2026-04
POST   /api/{communityKey}/expenses
PUT    /api/{communityKey}/expenses/{expenseKey}
DELETE /api/{communityKey}/expenses/{expenseKey}

# Presupuesto
GET    /api/{communityKey}/budgets?month=2026-04
PUT    /api/{communityKey}/budgets          ← upsert por categoría + período

# Balance
GET    /api/{communityKey}/balance?month=2026-04
GET    /api/{communityKey}/balance/annual?year=2026
```

---

## Implementación en dos partes

Este módulo se implementa en dos fases independientes.

---

## Parte A — Egresos y balance (Fase 1)

Lo mínimo para cerrar el ciclo financiero y habilitar reportes completos.

### Estructura de código

```
app/accounting/
  ├── ExpenseController.java
  ├── BalanceController.java
  └── dto/
      ├── CreateExpenseRequest.java
      ├── UpdateExpenseRequest.java
      ├── ExpenseResponse.java
      └── BalanceResponse.java

business/accounting/
  ├── ExpenseService.java              ← CRUD egresos + balance
  ├── ExpenseCategoryRepository.java   ← interfaz (puerto BD)
  ├── ExpenseRepository.java           ← interfaz (puerto BD)
  └── AccountingMapper.java

data/accounting/
  ├── ExpenseCategoryRepositoryImpl.java + JpaRepository + Entity
  └── ExpenseRepositoryImpl.java + JpaRepository + Entity
```

`ExpenseService.getBalance()` inyecta `ResidentPaymentRepository` y `PaymentAllocationRepository` directamente para calcular ingresos — no llama a otros Services.

### Integración con ingresos existentes

`ExpenseService.getBalance()` consulta ingresos desde:
- `ResidentPaymentRepository` filtrado por `paid_at` del período
- Cruzado con `PaymentAllocationRepository → FinancialObligationRepository` para saber el `origin_type` (COOPERATION o FINE)

No requiere tabla nueva para ingresos — los datos ya existen.

### Orden de implementación (Parte A)

- [ ] 1. Migración: `expense_categories` y `expenses`
- [ ] 2. Entidades + JpaRepositories + Repository interfaces + RepositoryImpls
- [ ] 3. DTOs + `AccountingMapper`
- [ ] 4. CRUD categorías + egresos — métodos en `ExpenseService`
- [ ] 5. `ExpenseService.getBalance()` — agrega ingresos desde `ResidentPaymentRepository`
- [ ] 6. `ExpenseController` + `BalanceController` + endpoints
- [ ] 7. Balance anual (agrupación por mes del año)

---

## Parte B — Presupuesto por categoría (Fase 4)

Planificación financiera. Requiere que los egresos ya estén implementados.

### Qué agrega

- Tabla `budgets` — presupuesto planificado por categoría y período
- `BudgetService` con upsert por categoría + período
- El balance ya existente incorpora `planned` vs `actual` por categoría
- Endpoints de gestión de presupuesto

### Estructura de código adicional

```
app/accounting/
  ├── BudgetController.java            ← nuevo
  └── dto/
      ├── UpsertBudgetRequest.java     ← nuevo
      └── BudgetResponse.java          ← nuevo

business/accounting/
  ├── BudgetService.java               ← nuevo
  └── BudgetRepository.java            ← nuevo (interfaz)

data/accounting/
  └── BudgetRepositoryImpl.java + JpaRepository + Entity  ← nuevo
```

### Orden de implementación (Parte B)

- [ ] 1. Migración: tabla `budgets`
- [ ] 2. `BudgetEntity` + `BudgetJpaRepository` + `BudgetRepository` + `BudgetRepositoryImpl`
- [ ] 3. DTOs de presupuesto
- [ ] 4. `BudgetService.upsert()`
- [ ] 5. Actualizar `ExpenseService.getBalance()` para incluir `planned` vs `actual`
- [ ] 6. `BudgetController` + endpoints

---

## Consideraciones

- El comprobante del gasto (`receipt_file`) reutiliza el módulo de archivos — `ExpenseService` inyecta `FileGateway` directamente
- El balance anual es útil para presentar en asambleas — se puede implementar junto con Parte A
- Agregar `registered_by` una vez que `plan-roles-autenticacion.md` esté implementado
