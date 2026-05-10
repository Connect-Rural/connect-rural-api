# Plan: Cooperaciones Recurrentes

## Contexto

Actualmente las cooperaciones son pagos únicos. Este plan extiende el modelo para soportar cooperaciones recurrentes (mensual, trimestral, anual), generando todas las obligations al momento de la creación con base en un rango de fechas (`startDate` → `endDate`).

---

## Reglas de negocio

| Situación | Regla |
|-----------|-------|
| Crear cooperación recurrente sin `endDate` | Error 400 |
| Actualizar `endDate` en cooperación recurrente | Error 400 — informar al usuario en el frontend al momento de crear |
| Eliminar residente asignado | Solo borra obligations con `status=PENDING` y `periodRef > hoy` |
| Agregar residente asignado | Solo genera obligations con `periodRef > hoy` |
| Pago ya registrado al eliminar residente | Se mantiene intacto |
| Periodicidad `DAILY` o `WEEKLY` | Rechazado — solo `MONTHLY`, `QUARTERLY`, `ANNUAL` |
| Más de 36 períodos calculados | Error 400 con mensaje explicativo |

---

## Cambios por capa

### 1. Base de datos — `0.0.7.sql`

**Tabla `connect_rural.cooperations`** — nuevas columnas:

```sql
periodicity  VARCHAR(20) NOT NULL DEFAULT 'ONE_TIME'
             CHECK (periodicity IN ('ONE_TIME','MONTHLY','QUARTERLY','ANNUAL'))
end_date     DATE NULL  -- obligatorio si periodicity != ONE_TIME
```

**Tabla `connect_rural.financial_obligations`** — nuevas columnas:

```sql
period_ref  DATE NULL  -- primer día del período: 2026-01-01, 2026-02-01...
due_date    DATE NULL  -- fecha límite de pago para ese período específico
```

> Para cooperaciones `ONE_TIME` ambas columnas quedan `NULL` — compatibilidad total con datos existentes.

---

### 2. Entidades

**`CooperationEntity`**
- `+ String periodicity` (default `"ONE_TIME"`)
- `+ LocalDate endDate`

**`FinancialObligationEntity`**
- `+ LocalDate periodRef`
- `+ LocalDate dueDate` — cada período tiene su propio vencimiento

---

### 3. DTOs

**`CreateCooperationRequest`**
- `+ CooperationPeriodicity periodicity` (default `ONE_TIME`)
- `+ LocalDate endDate` (requerido si periodicity != ONE_TIME, validar con `@AssertTrue`)

**`CooperationDetailResponse`** — reestructurar de lista plana a lista de períodos:

```
CooperationDetailResponse
  ├── info: { key, name, baseAmount, status, periodicity, ... }
  ├── totalPeriods: int
  ├── progress: double
  └── periods: List<PeriodSummary>
        ├── periodRef   "2026-01-01"
        ├── label       "Enero 2026"
        ├── dueDate
        ├── totalAssigned, totalPaid, totalPending
        ├── progress
        └── residents: List<ResidentAssignment>
```

> Para `ONE_TIME`, `periods` tiene un solo elemento — la UI puede renderizar ambos casos con el mismo componente.

**`ResidentAssignment`**
- `+ String periodRef` — necesario para que los endpoints de pay/unpay identifiquen la obligation correcta

**Nuevo enum `CooperationPeriodicity`** — vive plano en `business/cooperation/`:
```java
public enum CooperationPeriodicity {
    ONE_TIME, MONTHLY, QUARTERLY, ANNUAL
}
```

---

### 4. Cambios en Services

#### `CooperationService.create()`
```
Si periodicity == ONE_TIME → comportamiento actual (sin cambios)
Si periodicity != ONE_TIME:
  1. Validar que endDate != null
  2. Calcular períodos: startDate → endDate según periodicity
  3. Validar que el total de períodos <= 36
  4. Por cada período:
     - Calcular periodRef (primer día) y dueDate (último día del período o configurable)
     - financialObligationRepository.save(nueva obligation con periodRef y dueDate)
```

Extraer helper puro: `PeriodCalculator.calculate(startDate, endDate, periodicity) → List<LocalDate>` — vive plano en `business/cooperation/`.

#### `CooperationService.update()`
```
- Si se intenta modificar endDate en cooperación recurrente → lanzar IllegalStateException
- Si cambian residentes asignados:
    AGREGAR → solo crear obligations con periodRef > LocalDate.now()
    ELIMINAR → solo borrar obligations con status=PENDING y periodRef > LocalDate.now()
              las obligations PAID se mantienen intactas sin excepción
```

#### `CooperationService.getDetail()`
```
- Obtener todas las obligations de la cooperación via FinancialObligationRepository
- Agrupar por periodRef
- Para cada grupo construir PeriodSummary con sus ResidentAssignment
- Si periodicity == ONE_TIME → un solo PeriodSummary (comportamiento actual)
```

#### `CooperationService.markAsPaid()` / `markAsUnpaid()`
```
- Recibir periodRef como parámetro adicional
- Usar financialObligationRepository.findByCooperationAndResidentAndPeriod(cooperationKey, residentKey, periodRef)
- Para ONE_TIME, periodRef = null → buscar sin filtro de período (comportamiento actual)
```

`CooperationService` inyecta `FinancialObligationRepository` directamente para estas operaciones — no llama a `FinancialObligationService`.

Agregar en `FinancialObligationRepository` (interfaz):
```java
Optional<FinancialObligationEntity> findByCooperationAndResidentAndPeriod(
    UUID cooperationKey, UUID residentKey, LocalDate periodRef);
```

---

### 5. Controller — `CooperationController`

Endpoints de pay/unpay reciben `periodRef` como query param opcional:
```
PATCH /{cooperationKey}/residents/{residentKey}/pay?periodRef=2026-01-01
PATCH /{cooperationKey}/residents/{residentKey}/unpay?periodRef=2026-01-01
```

---

## Estructura de código

```
business/cooperation/
  ├── CooperationService.java         ← lógica de períodos integrada
  ├── CooperationRepository.java      ← interfaz (puerto BD)
  ├── CooperationPeriodicity.java     ← enum plano en el módulo
  ├── PeriodCalculator.java           ← helper puro, sin estado
  └── dto/
      └── PeriodSummary.java          ← modelo interno

business/financialobligation/
  ├── FinancialObligationRepository.java  ← nuevo método findByCooperationAndResidentAndPeriod

app/cooperation/
  └── dto/
      ├── CreateCooperationRequest.java   ← + periodicity, endDate
      └── CooperationDetailResponse.java  ← reestructurado con periods
```

---

## Orden de implementación

- [ ] 1. `0.0.7.sql` — agregar columnas a `cooperations` y `financial_obligations`
- [ ] 2. Actualizar entidades (`CooperationEntity`, `FinancialObligationEntity`)
- [ ] 3. Agregar enum `CooperationPeriodicity`
- [ ] 4. Actualizar DTOs request/response
- [ ] 5. Crear `PeriodCalculator` helper
- [ ] 6. Nuevo método en `FinancialObligationRepository`
- [ ] 7. `CooperationService.create()` — lógica de generación por períodos
- [ ] 8. `CooperationService.update()` — reglas de add/remove por período
- [ ] 9. `CooperationService.getDetail()` — agrupación por período
- [ ] 10. `CooperationService.markAsPaid()` / `markAsUnpaid()` — soporte de `periodRef`
- [ ] 11. `CooperationController` — `periodRef` como query param en pay/unpay
