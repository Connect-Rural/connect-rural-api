# Plan: Reportes y Exportación PDF/Excel

## Contexto

Todos los datos ya existen. Esta feature agrega endpoints que generan documentos descargables para administradores de comunidad.

---

## Reportes a implementar

| Reporte | Formato | Endpoint |
|---------|---------|----------|
| Estado de pagos de una cooperación | PDF / Excel | `GET /{communityKey}/cooperations/{cooperationKey}/report` |
| Reporte de morosidad por comunidad | PDF / Excel | `GET /{communityKey}/reports/delinquency` |
| Historial de pagos por residente | PDF | `GET /{communityKey}/residents/{residentKey}/report` |
| Resumen general de comunidad | PDF | `GET /{communityKey}/reports/summary` |

Query param: `?format=pdf` o `?format=excel`

---

## Dependencias a agregar (`pom.xml`)

```xml
<!-- PDF -->
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>itext7-core</artifactId>
    <version>7.2.5</version>
</dependency>

<!-- Excel -->
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.2.3</version>
</dependency>
```

---

## Estructura de código

```
business/report/
  ├── ReportService.java              ← orquesta datos inyectando Repositories directamente
  ├── pdf/
  │   ├── CooperationPdfGenerator.java
  │   └── DelinquencyPdfGenerator.java
  └── excel/
      ├── CooperationExcelGenerator.java
      └── DelinquencyExcelGenerator.java

app/report/
  └── ReportController.java
```

`ReportService` inyecta `CooperationRepository`, `ResidentRepository`, `FinancialObligationRepository` y `ResidentPaymentRepository` directamente — no llama a otros Services.

---

## Contenido de cada reporte

### Reporte de cooperación (PDF/Excel)

**Encabezado:**
- Nombre de cooperación, comunidad, fecha generación
- Monto base, recargo, fecha vencimiento
- Progreso: X/Y residentes pagaron (Z%)

**Tabla:**

| Residente | Estado | Monto Base | Recargo | Total | Fecha Pago | Referencia |
|-----------|--------|------------|---------|-------|------------|------------|
| Juan Pérez | PAGADO | ₡5,000 | ₡0 | ₡5,000 | 2026-03-15 | PAY-20260315-AB3K |
| Ana López | VENCIDO | ₡5,000 | ₡1,000 | ₡6,000 | — | — |

### Reporte de morosidad

- Lista de residentes con obligations en estado `PENDING` y `dueDate < hoy`
- Agrupado por cooperación
- Total adeudado por residente

### Historial de pagos por residente

- Todos los `resident_payments` del residente
- Con el nombre de la cooperación correspondiente (via `payment_allocations → financial_obligations`)
- Total pagado en el período

---

## Orden de implementación

- [ ] 1. Agregar dependencias PDF y Excel al `pom.xml`
- [ ] 2. `ReportService` — consulta y agrupa los datos via Repositories
- [ ] 3. `CooperationPdfGenerator`
- [ ] 4. `CooperationExcelGenerator`
- [ ] 5. `ReportController` con los 4 endpoints
- [ ] 6. `DelinquencyPdfGenerator` + `DelinquencyExcelGenerator`
- [ ] 7. `ResidentHistoryPdfGenerator`

---

## Consideraciones

- Los archivos no deben guardarse en disco ni en MinIO — generarlos en memoria y streamear la respuesta (`StreamingResponseBody`)
- `Content-Disposition: attachment; filename="reporte-cooperacion-{nombre}-{fecha}.pdf"`
- Para Excel usar `.xlsx` (POI OOXML), no el formato `.xls` legacy
