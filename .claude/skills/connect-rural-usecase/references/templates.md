# Use Case Templates

Replace `{Module}`, `{module}`, `{Action}` with the actual names.

---

## Simple use case (single service, no @Transactional)

```java
package com.crdev.connect_rural_api.business.{module}.usecases;

import com.crdev.connect_rural_api.app.{module}.dto.response.{Module}ResponseDto;
import com.crdev.connect_rural_api.business.{module}.{Module}Service;
import com.crdev.connect_rural_api.business.{module}.mapper.{Module}AppMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class {Action}{Module}UseCase {
    private final {Module}Service service;
    private final {Module}AppMapper mapper;

    public {Module}ResponseDto execute(String communityKey, String key) {
        var entity = service.getByKey(communityKey, key);
        return mapper.toResponse(entity);
    }
}
```

---

## Aggregate use case (multiple services, @Transactional read)

Based on `GetCooperationDetailByKeyUseCase`:

```java
package com.crdev.connect_rural_api.business.{module}.usecases;

import com.crdev.connect_rural_api.app.{module}.dto.response.{Module}DetailResponseDto;
import com.crdev.connect_rural_api.business.{module}.{Module}Service;
import com.crdev.connect_rural_api.business.{module}.mapper.{Module}AppMapper;
import com.crdev.connect_rural_api.business.{related}.{Related}Service;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class Get{Module}DetailByKeyUseCase {
    private final {Module}Service service;
    private final {Related}Service relatedService;
    private final {Module}AppMapper mapper;

    @Transactional
    public {Module}DetailResponseDto execute(String communityKey, String key) {
        // 1. Load primary entity
        var entity = service.getByKey(communityKey, key);

        // 2. Load related data
        var relatedItems = relatedService.listBy{Module}(key);

        // 3. Calculate statistics
        int total = relatedItems.size();
        int paid = (int) relatedItems.stream().filter(r -> r.getIsPaid()).count();
        int pending = total - paid;
        double progress = total > 0 ? (paid * 100.0) / total : 0.0;

        // 4. Map to enriched response
        return mapper.toDetailResponseDto(entity, relatedItems, progress, total, paid, pending);
    }
}
```

---

## Transactional write use case (multi-service write, @Transactional)

Based on `CreateCooperationUseCase`:

```java
@Component
@AllArgsConstructor
public class Create{Module}UseCase {
    private final {Module}Service service;
    private final {Related}Service relatedService;
    private final {Module}AppMapper mapper;

    @Transactional
    public {Module}ResponseDto execute(String communityKey, Create{Module}Dto dto) {
        // 1. Build and persist main entity
        var entity = new {Module}Entity(
                null,
                UUID.fromString(communityKey),
                dto.getName(),
                // ... other fields
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        var saved = service.create(entity);

        // 2. Perform related writes (will rollback if this fails)
        relatedService.assignTo{Module}(saved.getKey().toString(), dto.getRelatedKeys());

        return mapper.toResponse(saved);
    }
}
```

---

## Controller wiring (adding to existing controller)

Add the field and the endpoint to the existing `{Module}Controller`:

```java
// 1. Add field (Lombok @RequiredArgsConstructor injects it)
private final Get{Module}DetailByKeyUseCase get{Module}DetailByKeyUC;

// 2. Add endpoint
@GetMapping("/{key}/detail")
public ResponseEntity<{Module}DetailResponseDto> getDetail(
        @PathVariable String communityKey,
        @PathVariable String key) {
    return ResponseEntity.ok(get{Module}DetailByKeyUC.execute(communityKey, key));
}
```

For POST actions on a specific resource:

```java
@PostMapping("/{key}/{action}")
public ResponseEntity<{Module}ResponseDto> performAction(
        @PathVariable String communityKey,
        @PathVariable String key,
        @Valid @RequestBody {Action}RequestDto request) {
    return ResponseEntity.ok({action}{Module}UC.execute(communityKey, key, request));
}
```

---

## Mapper method for enriched response

Add to `{Module}AppMapper.java` when the use case needs a new mapping:

```java
public {Module}DetailResponseDto toDetailResponseDto(
        {Module}Entity entity,
        List<RelatedItem> items,
        double progress,
        int total,
        int paid,
        int pending) {
    if (entity == null) return null;
    return new {Module}DetailResponseDto(
            entity.getKey().toString(),
            entity.getName(),
            items,
            progress,
            total,
            paid,
            pending
    );
}
```