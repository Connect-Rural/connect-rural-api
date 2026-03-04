# Code Templates

Replace `{Module}` / `{module}` / `{table_name}` with the actual domain name.
For community-scoped modules add `communityKey` filtering as shown in the Specs and Service examples.

---

## Entity

```java
package com.crdev.connect_rural_api.data.{module};

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "{table_name}")
public class {Module}Entity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "{module}_key", nullable = false)
    private UUID key;

    // community-scoped modules: add this field
    @Column(name = "community_key", nullable = false)
    private UUID communityKey;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "active")
    private Boolean active;

    @Column(name = "created_at", updatable = false)
    private Date createdAt;

    @Column(name = "updated_at")
    private Date updatedAt;
}
```

---

## Repository

```java
package com.crdev.connect_rural_api.data.{module};

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.UUID;

public interface {Module}Repository extends JpaRepository<{Module}Entity, UUID>,
        JpaSpecificationExecutor<{Module}Entity> {
}
```

---

## Specs (community-scoped + keyword filter)

```java
package com.crdev.connect_rural_api.business.{module}.specs;

import com.crdev.connect_rural_api.data.{module}.{Module}Entity;
import org.springframework.data.jpa.domain.Specification;
import java.util.UUID;

public class {Module}Specs {

    public static Specification<{Module}Entity> withCommunity(UUID communityKey) {
        return (root, query, cb) ->
                cb.equal(root.get("communityKey"), communityKey);
    }

    public static Specification<{Module}Entity> withKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return cb.conjunction();
            String like = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), like)
            );
        };
    }
}
```

---

## Service

```java
package com.crdev.connect_rural_api.business.{module};

import com.crdev.connect_rural_api.business.{module}.specs.{Module}Specs;
import com.crdev.connect_rural_api.data.{module}.{Module}Entity;
import com.crdev.connect_rural_api.data.{module}.{Module}Repository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class {Module}Service {

    private final {Module}Repository repository;

    public List<{Module}Entity> getAll() {
        return repository.findAll();
    }

    // community-scoped version:
    public Page<{Module}Entity> getAllPaginatedAndFiltered(String communityKey, String keyword, int page, int size) {
        Specification<{Module}Entity> spec = Specification
                .where({Module}Specs.withCommunity(UUID.fromString(communityKey)))
                .and({Module}Specs.withKeyword(keyword));
        return repository.findAll(spec, PageRequest.of(page, size));
    }

    public {Module}Entity getByKey(String key) {
        return repository.findById(UUID.fromString(key))
                .orElseThrow(() -> new IllegalArgumentException("{Module} not found"));
    }

    // community-scoped version:
    public {Module}Entity getByCommunityKeyAndKey(String communityKey, String key) {
        Specification<{Module}Entity> spec = Specification
                .where({Module}Specs.withCommunity(UUID.fromString(communityKey)))
                .and((root, query, cb) -> cb.equal(root.get("key"), UUID.fromString(key)));
        return repository.findOne(spec)
                .orElseThrow(() -> new IllegalArgumentException("{Module} not found"));
    }

    public {Module}Entity create({Module}Entity entity) {
        return repository.save(entity);
    }

    public {Module}Entity update({Module}Entity entity) {
        return repository.save(entity);
    }

    public void delete(String key) {
        repository.deleteById(UUID.fromString(key));
    }
}
```

---

## Mapper

```java
package com.crdev.connect_rural_api.business.{module}.mapper;

import com.crdev.connect_rural_api.app.{module}.dto.request.Create{Module}Dto;
import com.crdev.connect_rural_api.app.{module}.dto.response.{Module}ResponseDto;
import com.crdev.connect_rural_api.data.{module}.{Module}Entity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class {Module}AppMapper {

    public {Module}ResponseDto toResponse({Module}Entity entity) {
        if (entity == null) return null;
        return new {Module}ResponseDto(
                entity.getKey().toString(),
                entity.getName(),
                entity.getActive()
        );
    }

    public List<{Module}ResponseDto> toResponseList(List<{Module}Entity> entities) {
        if (entities == null) return null;
        return entities.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public {Module}Entity updateFromDto(Create{Module}Dto dto, {Module}Entity entity) {
        if (dto == null || entity == null) return entity;
        entity.setName(dto.getName());
        return entity;
    }
}
```

---

## Use Cases

### Create{Module}UseCase

```java
package com.crdev.connect_rural_api.business.{module}.usecases;

import com.crdev.connect_rural_api.app.{module}.dto.request.Create{Module}Dto;
import com.crdev.connect_rural_api.app.{module}.dto.response.{Module}ResponseDto;
import com.crdev.connect_rural_api.business.{module}.{Module}Service;
import com.crdev.connect_rural_api.business.{module}.mapper.{Module}AppMapper;
import com.crdev.connect_rural_api.data.{module}.{Module}Entity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
@AllArgsConstructor
public class Create{Module}UseCase {
    private final {Module}Service service;
    private final {Module}AppMapper mapper;

    public {Module}ResponseDto execute(String communityKey, Create{Module}Dto dto) {
        var entity = new {Module}Entity(
                null,
                UUID.fromString(communityKey),
                dto.getName(),
                true,
                null,
                null
        );
        return mapper.toResponse(service.create(entity));
    }
}
```

### Get{Module}PaginatedUseCase

```java
@Component
@AllArgsConstructor
public class Get{Module}PaginatedUseCase {
    private final {Module}Service service;
    private final {Module}AppMapper mapper;

    public {Module}PaginatedResponseDto execute(String communityKey, {Module}FilterDto filter) {
        var page = service.getAllPaginatedAndFiltered(
                communityKey, filter.getKeyword(), filter.getPage(), filter.getSize());
        return new {Module}PaginatedResponseDto(
                mapper.toResponseList(page.getContent()),
                filter.getPage(),
                filter.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
```

### Update{Module}UseCase

```java
@Component
@AllArgsConstructor
public class Update{Module}UseCase {
    private final {Module}Service service;
    private final {Module}AppMapper mapper;

    public {Module}ResponseDto execute(String communityKey, String key, Create{Module}Dto dto) {
        var entity = service.getByCommunityKeyAndKey(communityKey, key);
        mapper.updateFromDto(dto, entity);
        return mapper.toResponse(service.update(entity));
    }
}
```

### Delete{Module}UseCase

```java
@Component
@AllArgsConstructor
public class Delete{Module}UseCase {
    private final {Module}Service service;

    public void execute(String key) {
        service.delete(key);
    }
}
```

---

## Request DTOs

### Create{Module}Dto

```java
package com.crdev.connect_rural_api.app.{module}.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Create{Module}Dto {
    @NotBlank(message = "El nombre es obligatorio")
    private String name;
}
```

### {Module}FilterDto

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class {Module}FilterDto {
    private String keyword;
    private Integer page;
    private Integer size;
}
```

---

## Response DTOs

### {Module}ResponseDto

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class {Module}ResponseDto {
    private String key;
    private String name;
    private Boolean active;
}
```

### {Module}PaginatedResponseDto

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class {Module}PaginatedResponseDto {
    private List<{Module}ResponseDto> data;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
```

---

## Controller (community-scoped)

```java
package com.crdev.connect_rural_api.app.{module};

import com.crdev.connect_rural_api.app.{module}.dto.request.Create{Module}Dto;
import com.crdev.connect_rural_api.app.{module}.dto.request.{Module}FilterDto;
import com.crdev.connect_rural_api.app.{module}.dto.response.{Module}PaginatedResponseDto;
import com.crdev.connect_rural_api.app.{module}.dto.response.{Module}ResponseDto;
import com.crdev.connect_rural_api.business.{module}.usecases.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/{communityKey}/{modules}")
@RequiredArgsConstructor
public class {Module}Controller {

    private final Create{Module}UseCase createUseCase;
    private final Get{Module}ListUseCase listUseCase;
    private final Get{Module}PaginatedUseCase paginatedUseCase;
    private final Get{Module}ByKeyUseCase getByKeyUseCase;
    private final Update{Module}UseCase updateUseCase;
    private final Delete{Module}UseCase deleteUseCase;

    @PostMapping
    public ResponseEntity<{Module}ResponseDto> create(
            @PathVariable String communityKey,
            @Valid @RequestBody Create{Module}Dto request) {
        return ResponseEntity.status(201).body(createUseCase.execute(communityKey, request));
    }

    @GetMapping
    public ResponseEntity<List<{Module}ResponseDto>> list(@PathVariable String communityKey) {
        return ResponseEntity.ok(listUseCase.execute(communityKey));
    }

    @GetMapping("/paginated")
    public ResponseEntity<{Module}PaginatedResponseDto> getPaginated(
            @PathVariable String communityKey,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                paginatedUseCase.execute(communityKey, new {Module}FilterDto(keyword, page, size))
        );
    }

    @GetMapping("/{key}")
    public ResponseEntity<{Module}ResponseDto> getByKey(
            @PathVariable String communityKey,
            @PathVariable String key) {
        return ResponseEntity.ok(getByKeyUseCase.execute(communityKey, key));
    }

    @PatchMapping("/{key}")
    public ResponseEntity<{Module}ResponseDto> update(
            @PathVariable String communityKey,
            @PathVariable String key,
            @Valid @RequestBody Create{Module}Dto request) {
        return ResponseEntity.ok(updateUseCase.execute(communityKey, key, request));
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<?> delete(
            @PathVariable String communityKey,
            @PathVariable String key) {
        deleteUseCase.execute(key);
        return ResponseEntity.noContent().build();
    }
}
```

---

## Migration SQL changeset

```xml
<!-- In db.changelog-master.xml -->
<include file="db/changelog/scripts/{version}.sql" relativeToChangelogFile="false"/>
```

```sql
-- {version}.sql
CREATE TABLE {table_name} (
    {module}_key   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    community_key  UUID NOT NULL REFERENCES communities(community_key) ON DELETE CASCADE,
    name           VARCHAR(200) NOT NULL,
    active         BOOLEAN DEFAULT TRUE,
    created_at     TIMESTAMP DEFAULT NOW(),
    updated_at     TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_{table_name}_community ON {table_name}(community_key);
```