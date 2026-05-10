# Plan: Módulo de Documentos

## Contexto

El módulo de archivos existente (MinIO) almacena archivos genéricos sin contexto. Este módulo agrega tres capas sobre eso:

1. **Repositorio de documentos** — documentos oficiales de la comunidad (actas, reglamentos, contratos) con categorías, versiones y acceso controlado.
2. **Generación desde plantillas** — producir documentos PDF a partir de plantillas predefinidas con datos del sistema (carta de deuda, constancia de residente, recibo de pago, acta de asamblea).
3. **Solicitudes de documentos** — flujo donde un residente solicita un documento oficial que debe ser revisado y emitido formalmente por la autoridad de la comunidad (presidente, secretario, etc.). El sistema genera el PDF listo para **imprimir, firmar y sellar físicamente** por el admin. No requiere firma digital — el documento impreso es el original oficial.
4. **Verificación pública** — código QR en el PDF que permite a terceros verificar la autenticidad del documento escaneando desde cualquier dispositivo.
5. **Extras del flujo** — adjuntos en solicitudes, comentarios admin↔residente, enlace temporal de descarga, notificaciones de vencimiento, emisión múltiple.

---

## Parte 1: Repositorio de documentos

### Modelo de datos

#### `document_categories` — tipos de documento

```sql
CREATE TABLE connect_rural.document_categories (
    category_key    UUID PRIMARY KEY DEFAULT public.uuid_generate_v4(),
    community_key   UUID NOT NULL,
    name            VARCHAR(100) NOT NULL,   -- Actas, Reglamentos, Contratos, Correspondencia
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_doccat_community FOREIGN KEY (community_key)
        REFERENCES connect_rural.communities(community_key) ON DELETE CASCADE
);
```

#### `documents` — documentos oficiales

```sql
CREATE TABLE connect_rural.documents (
    document_key    UUID PRIMARY KEY DEFAULT public.uuid_generate_v4(),
    community_key   UUID NOT NULL,
    category_key    UUID NOT NULL,
    title           VARCHAR(255) NOT NULL,
    description     TEXT NULL,
    file_key        UUID NOT NULL,              -- referencia a file_objects (MinIO)
    version         INT NOT NULL DEFAULT 1,
    origin_type     VARCHAR(50) NULL,           -- ASSEMBLY | GENERATED | MANUAL | ISSUED
    origin_id       UUID NULL,                  -- assembly_key, doc_request_key, etc.
    uploaded_by     UUID NULL,                  -- user_key
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_doc_community FOREIGN KEY (community_key)
        REFERENCES connect_rural.communities(community_key) ON DELETE CASCADE,
    CONSTRAINT fk_doc_category  FOREIGN KEY (category_key)
        REFERENCES connect_rural.document_categories(category_key) ON DELETE RESTRICT
);
CREATE INDEX IF NOT EXISTS idx_doc_community_category ON connect_rural.documents(community_key, category_key);
CREATE INDEX IF NOT EXISTS idx_doc_origin ON connect_rural.documents(origin_type, origin_id);
```

### Endpoints

```
GET    /api/{communityKey}/documents?category=&keyword=
POST   /api/{communityKey}/documents          ← upload manual
GET    /api/{communityKey}/documents/{documentKey}
DELETE /api/{communityKey}/documents/{documentKey}
```

---

## Parte 2: Generación desde plantillas

### Tipos de documento generables

| Tipo | Descripción | Variables principales |
|------|-------------|----------------------|
| `DEBT_NOTICE` | Carta de cobro a residente moroso | residente, cooperación, monto, vencimiento |
| `RESIDENT_CERTIFICATE` | Constancia de que es residente activo | residente, comunidad, fecha |
| `PAYMENT_RECEIPT` | Recibo de pago de cooperación | residente, cooperación, monto, fecha, referencia |
| `ASSEMBLY_MINUTES` | Acta formal de asamblea | asamblea, asistentes, acuerdos, votos |
| `ISSUED_DOCUMENT` | Documento emitido por solicitud del residente | residente, folio, propósito, autoridad firmante |
| `CUSTOM` | Plantilla libre definida por el admin | variables libres `{{variable}}` |

### Modelo de datos

#### `document_templates` — plantillas por comunidad

```sql
CREATE TABLE connect_rural.document_templates (
    template_key        UUID PRIMARY KEY DEFAULT public.uuid_generate_v4(),
    community_key       UUID NOT NULL,
    name                VARCHAR(150) NOT NULL,
    template_type       VARCHAR(50) NOT NULL,
    content             TEXT NOT NULL,           -- HTML con placeholders {{variable}}
    requires_approval   BOOLEAN NOT NULL DEFAULT FALSE,
    active              BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_template_type CHECK (template_type IN
        ('DEBT_NOTICE','RESIDENT_CERTIFICATE','PAYMENT_RECEIPT','ASSEMBLY_MINUTES','ISSUED_DOCUMENT','CUSTOM')),
    CONSTRAINT fk_template_community FOREIGN KEY (community_key)
        REFERENCES connect_rural.communities(community_key) ON DELETE CASCADE
);
```

### Endpoints de generación

```
# Gestión de plantillas
GET    /api/{communityKey}/documents/templates
POST   /api/{communityKey}/documents/templates
PUT    /api/{communityKey}/documents/templates/{templateKey}
DELETE /api/{communityKey}/documents/templates/{templateKey}

# Generación directa (admin)
POST   /api/{communityKey}/documents/generate
  Body: {
    templateKey,
    context: {
      residentKey?,
      cooperationKey?,
      assemblyKey?,
      customVars?: { "nombreVariable": "valor" }
    },
    saveToRepository: true
  }
  Response: PDF stream o { documentKey }
```

### Variables disponibles por tipo

**`DEBT_NOTICE`:**
```
{{residentName}}, {{residentPhone}}, {{communityName}}
{{cooperationName}}, {{baseAmount}}, {{lateFee}}, {{totalAmount}}
{{dueDate}}, {{generatedDate}}
```

**`RESIDENT_CERTIFICATE`:**
```
{{residentName}}, {{residentId}}, {{communityName}}
{{residentSince}}, {{generatedDate}}, {{adminName}}
```

**`PAYMENT_RECEIPT`:**
```
{{residentName}}, {{cooperationName}}, {{amountPaid}}
{{paidAt}}, {{paymentReference}}, {{paymentMethod}}, {{communityName}}
```

**`ASSEMBLY_MINUTES`:**
```
{{assemblyTitle}}, {{assemblyDate}}, {{location}}
{{attendeesList}}, {{quorumReached}}, {{agreementsList}}
{{minutesContent}}, {{communityName}}
```

**`ISSUED_DOCUMENT`:**
```
{{residentName}}, {{residentId}}, {{communityName}}
{{documentPurpose}}, {{folioNumber}}, {{issuedDate}}
{{authorityName}}, {{authorityRole}}, {{communityAddress}}
```

---

## Parte 3: Solicitudes de documentos oficiales

### Contexto

Un residente necesita un documento emitido por la autoridad local para un trámite externo (banco, municipalidad, migración, etc.). El flujo es:

```
Residente solicita → Admin revisa → Admin aprueba y genera → Documento emitido con folio → Entrega al residente
```

### Modelo de datos

#### `document_request_types` — tipos de solicitud configurables por comunidad

```sql
CREATE TABLE connect_rural.document_request_types (
    type_key        UUID PRIMARY KEY DEFAULT public.uuid_generate_v4(),
    community_key   UUID NOT NULL,
    name            VARCHAR(150) NOT NULL,
    description     TEXT NULL,
    template_key    UUID NOT NULL,
    requires_no_debt BOOLEAN NOT NULL DEFAULT FALSE,
    delivery_days   INT NOT NULL DEFAULT 3,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_reqtype_community FOREIGN KEY (community_key)
        REFERENCES connect_rural.communities(community_key) ON DELETE CASCADE,
    CONSTRAINT fk_reqtype_template  FOREIGN KEY (template_key)
        REFERENCES connect_rural.document_templates(template_key) ON DELETE RESTRICT
);
```

#### `document_requests` — solicitudes de residentes

```sql
CREATE TABLE connect_rural.document_requests (
    request_key     UUID PRIMARY KEY DEFAULT public.uuid_generate_v4(),
    community_key   UUID NOT NULL,
    resident_key    UUID NOT NULL,
    type_key        UUID NOT NULL,
    purpose         TEXT NOT NULL,
    status          VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    folio_number    VARCHAR(50) NULL,
    rejection_note  TEXT NULL,
    reviewed_by     UUID NULL,
    reviewed_at     TIMESTAMP NULL,
    document_key    UUID NULL,
    expires_at      DATE NULL,
    download_token      VARCHAR(100) NULL,
    download_token_exp  TIMESTAMP NULL,
    download_count      INT NOT NULL DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_req_status CHECK (status IN ('PENDING','APPROVED','REJECTED','DELIVERED')),
    CONSTRAINT fk_docreq_community FOREIGN KEY (community_key)
        REFERENCES connect_rural.communities(community_key) ON DELETE CASCADE,
    CONSTRAINT fk_docreq_resident  FOREIGN KEY (resident_key)
        REFERENCES connect_rural.residents(resident_key) ON DELETE CASCADE,
    CONSTRAINT fk_docreq_type      FOREIGN KEY (type_key)
        REFERENCES connect_rural.document_request_types(type_key) ON DELETE RESTRICT,
    CONSTRAINT fk_docreq_document  FOREIGN KEY (document_key)
        REFERENCES connect_rural.documents(document_key) ON DELETE SET NULL
);
CREATE INDEX IF NOT EXISTS idx_docreq_community_status ON connect_rural.document_requests(community_key, status);
CREATE INDEX IF NOT EXISTS idx_docreq_resident ON connect_rural.document_requests(resident_key);
```

### Flujo de estados

```
PENDING → APPROVED → DELIVERED
    ↓
REJECTED
```

### Folio automático

Al aprobar, generar folio con formato `{YYYY}-{secuencial}` por comunidad:
```
2026-001, 2026-002, 2026-003...
```
Reinicia en `001` cada año.

### Validaciones al aprobar

- Si `requires_no_debt = true`: verificar via `FinancialObligationRepository` que el residente no tenga obligations PENDING con `dueDate < hoy`

### Endpoints

```
# Tipos de solicitud (admin configura)
GET    /api/{communityKey}/documents/request-types
POST   /api/{communityKey}/documents/request-types
PUT    /api/{communityKey}/documents/request-types/{typeKey}

# Solicitudes (admin gestiona)
GET    /api/{communityKey}/documents/requests?status=PENDING
GET    /api/{communityKey}/documents/requests/{requestKey}
PATCH  /api/{communityKey}/documents/requests/{requestKey}/approve
PATCH  /api/{communityKey}/documents/requests/{requestKey}/reject
PATCH  /api/{communityKey}/documents/requests/{requestKey}/deliver

# Desde portal del residente
GET    /api/portal/documents/request-types
POST   /api/portal/documents/requests
GET    /api/portal/documents/requests
GET    /api/portal/documents/requests/{requestKey}/download

# Verificación pública (sin auth)
GET    /public/verify/{folioNumber}
GET    /public/documents/download?token=ABC123XYZ
```

---

## Motor de plantillas

Usar **Thymeleaf** para renderizar HTML con las variables, luego convertir a PDF con **iText7**.

```
content (HTML con {{vars}})
  → Thymeleaf → HTML renderizado
  → iText7   → PDF con folio, fecha, nombre de autoridad
  → MinIO    → guardado como document (via FileGateway)
  → WhatsApp → link de descarga (opcional, via WhatsappGateway)
```

---

## Estructura de código

```
app/document/
  ├── DocumentController.java
  ├── DocumentTemplateController.java
  ├── DocumentRequestController.java
  ├── DocumentVerificationController.java     ← público, sin auth
  └── dto/
      ├── UploadDocumentRequest.java
      ├── CreateTemplateRequest.java
      ├── GenerateDocumentRequest.java
      ├── CreateDocumentRequestRequest.java
      ├── ApproveDocumentRequestRequest.java
      ├── RejectDocumentRequestRequest.java
      ├── DocumentResponse.java
      ├── DocumentRequestResponse.java
      └── DocumentRequestTypeResponse.java

business/document/
  ├── DocumentService.java                    ← upload, list, delete, generate
  ├── DocumentTemplateService.java            ← CRUD plantillas
  ├── DocumentRequestService.java             ← create, approve, reject, deliver
  ├── DocumentCategoryRepository.java         ← interfaz (puerto BD)
  ├── DocumentRepository.java                 ← interfaz (puerto BD)
  ├── DocumentTemplateRepository.java         ← interfaz (puerto BD)
  ├── DocumentRequestTypeRepository.java      ← interfaz (puerto BD)
  ├── DocumentRequestRepository.java          ← interfaz (puerto BD)
  ├── DocumentRequestAttachmentRepository.java
  ├── DocumentRequestCommentRepository.java
  ├── DocumentMapper.java
  └── generator/
      ├── DocumentGeneratorService.java       ← Thymeleaf + iText + QR
      ├── TemplateVariableResolver.java       ← resuelve variables según context
      ├── FolioGeneratorService.java          ← genera folio secuencial por año
      ├── QrCodeService.java                  ← genera imagen QR con ZXing
      ├── BulkDocumentGeneratorService.java   ← genera ZIP con múltiples PDFs
      └── templates/
          ├── debt-notice.html
          ├── resident-certificate.html
          ├── payment-receipt.html
          ├── assembly-minutes.html
          └── issued-document.html

data/document/
  ├── DocumentCategoryRepositoryImpl.java + JpaRepository + Entity
  ├── DocumentRepositoryImpl.java + JpaRepository + Entity
  ├── DocumentTemplateRepositoryImpl.java + JpaRepository + Entity
  ├── DocumentRequestTypeRepositoryImpl.java + JpaRepository + Entity
  ├── DocumentRequestRepositoryImpl.java + JpaRepository + Entity
  ├── DocumentRequestAttachmentRepositoryImpl.java + JpaRepository + Entity
  └── DocumentRequestCommentRepositoryImpl.java + JpaRepository + Entity
```

`DocumentService` inyecta `FileGateway` para guardar PDFs en MinIO — no llama a `FileService`.
`DocumentRequestService.approve()` inyecta `FinancialObligationRepository` para validar deudas — no llama a `FinancialObligationService`.
`DocumentRequestService.deliver()` inyecta `WhatsappGateway` para notificar — no llama a `WhatsappService`.

---

## Parte 4: Verificación pública por QR

El PDF incluye un código QR apuntando a:
```
GET /public/verify/{folioNumber}
```

**Respuesta:**
```json
{
  "valid": true,
  "folio": "2026-042",
  "documentType": "Constancia de Residencia",
  "residentName": "Juan Pérez",
  "communityName": "Comunidad El Roble",
  "issuedAt": "2026-04-01",
  "expiresAt": "2026-07-01",
  "status": "VALID"
}
```

### Generación del QR

```xml
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>core</artifactId>
    <version>3.5.2</version>
</dependency>
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>javase</artifactId>
    <version>3.5.2</version>
</dependency>
```

---

## Parte 5: Extras del flujo de solicitudes

### Adjuntos en solicitudes

```sql
CREATE TABLE connect_rural.document_request_attachments (
    attachment_key  UUID PRIMARY KEY DEFAULT public.uuid_generate_v4(),
    request_key     UUID NOT NULL,
    file_key        UUID NOT NULL,
    description     VARCHAR(150) NULL,
    uploaded_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reqattach_request FOREIGN KEY (request_key)
        REFERENCES connect_rural.document_requests(request_key) ON DELETE CASCADE
);
```

### Comentarios admin ↔ residente

```sql
CREATE TABLE connect_rural.document_request_comments (
    comment_key     UUID PRIMARY KEY DEFAULT public.uuid_generate_v4(),
    request_key     UUID NOT NULL,
    author_type     VARCHAR(10) NOT NULL,   -- ADMIN | RESIDENT
    author_key      UUID NOT NULL,
    message         TEXT NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_author_type CHECK (author_type IN ('ADMIN','RESIDENT')),
    CONSTRAINT fk_reqcomment_request FOREIGN KEY (request_key)
        REFERENCES connect_rural.document_requests(request_key) ON DELETE CASCADE
);
```

### Emisión múltiple (bulk)

```
POST /api/{communityKey}/documents/generate-bulk
  Body: {
    templateKey,
    residentKeys: [...],
    sendWhatsapp: true
  }
  Response: { jobId }  ← proceso asíncrono
```

---

## Orden de implementación

- [ ] 1. Migración: todas las tablas del módulo
- [ ] 2. Entidades + JpaRepositories + Repository interfaces + RepositoryImpls
- [ ] 3. DTOs + `DocumentMapper`
- [ ] 4. CRUD repositorio de documentos (upload manual)
- [ ] 5. CRUD plantillas
- [ ] 6. `FolioGeneratorService` — secuencial por año por comunidad
- [ ] 7. `QrCodeService` — genera imagen QR con ZXing
- [ ] 8. `TemplateVariableResolver` — resuelve variables según context
- [ ] 9. `DocumentGeneratorService` — Thymeleaf + iText + QR embebido
- [ ] 10. Plantillas HTML base para los 5 tipos estándar
- [ ] 11. `DocumentVerificationController` + endpoint público `/public/verify/{folio}`
- [ ] 12. CRUD tipos de solicitud
- [ ] 13. `DocumentRequestService.create()` + adjuntos
- [ ] 14. `DocumentRequestService.approve()` — validar deuda + generar PDF + folio + token descarga
- [ ] 15. `DocumentRequestService.reject()`
- [ ] 16. `DocumentRequestService.deliver()` — enlace temporal + WhatsApp
- [ ] 17. Comentarios admin ↔ residente
- [ ] 18. Endpoints portal del residente
- [ ] 19. Job de notificación de vencimiento
- [ ] 20. `BulkDocumentGeneratorService` + ZIP
- [ ] 21. Integración: acta al cerrar asamblea
- [ ] 22. Integración: recibo al marcar cooperación como pagada

---

## Consideraciones

- El folio es único por comunidad y año — usar transacción con `SELECT MAX + 1` o secuencia en BD para evitar duplicados bajo concurrencia
- Los documentos emitidos tienen `expires_at` configurable por tipo (30, 60, 90 días)
- El enlace temporal de descarga expira en 48h o tras la primera descarga — lo que ocurra primero
- La plantilla `issued-document.html` debe dejar espacio visual claro para firma y sello físico
- La plantilla `CUSTOM` permite variables libres — el frontend extrae los `{{placeholders}}` del HTML para mostrarlos como campos editables
- Las plantillas base del sistema viven en `resources/templates/documents/` — no en BD
- Versionar documentos: nueva versión incrementa `version`, la anterior queda inactiva (no se borra)
- `requires_no_debt` es por decisión de cada comunidad — no es una regla global
- La emisión bulk es asíncrona — notificar al admin por WebSocket o polling cuando el ZIP esté listo
