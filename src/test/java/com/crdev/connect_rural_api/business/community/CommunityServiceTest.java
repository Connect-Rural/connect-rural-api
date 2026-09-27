package com.crdev.connect_rural_api.business.community;

import com.crdev.connect_rural_api.business.shared.PageParams;
import com.crdev.connect_rural_api.business.shared.PagedResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios del dominio comunidad: sin Spring, sin H2, sin JPA.
 *
 * Justifica la separación de capas de la Opción B: el Service solo depende
 * del puerto {@link CommunityRepository}, así que basta un fake en memoria
 * para ejercitar toda la lógica de negocio.
 */
class CommunityServiceTest {

    private InMemoryCommunityRepository repository;
    private CommunityService service;

    @BeforeEach
    void setUp() {
        repository = new InMemoryCommunityRepository();
        service = new CommunityService(repository);
    }

    private static Community draft(String name) {
        Community community = new Community();
        community.setName(name);
        community.setDescription("desc");
        community.setLogoUrl("logo.png");
        community.setSubscriptionPlan("FREE");
        return community;
    }

    @Test
    @DisplayName("create fija los valores por defecto y limpia el borrador")
    void createAppliesDefaults() {
        Community draft = draft("Los Naranjos");
        UUID garbageKey = UUID.randomUUID();
        draft.setKey(garbageKey);          // basura en el borrador
        draft.setActive(false);
        draft.setCompletedConfiguration(true);

        Community created = service.create(draft);

        assertNotEquals(garbageKey, created.getKey(), "la clave del borrador se descarta y asigna la persistencia");
        assertEquals(Boolean.TRUE, created.getActive(), "comunidad nueva = activa");
        assertEquals(Boolean.FALSE, created.getCompletedConfiguration(), "configuración pendiente");
        assertEquals("Los Naranjos", created.getName());
        assertEquals(1, repository.stored.size());
    }

    @Test
    @DisplayName("getByKey con clave inexistente lanza 'Community not found'")
    void getByKeyUnknownThrows() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.getByKey(UUID.randomUUID().toString()));
        assertEquals("Community not found", ex.getMessage());
    }

    @Test
    @DisplayName("update solo modifica los campos editables")
    void updateOnlyTouchesEditableFields() {
        Community saved = repository.save(draft("Original"));
        saved.setActive(false);
        saved.setSubscriptionPlan("PREMIUM");
        UUID appKey = UUID.randomUUID();
        saved.setWhatsappAppKey(appKey);

        Community changes = draft("Renombrada");
        changes.setSubscriptionPlan("FREE");
        Community updated = service.update(saved.getKey().toString(), changes);

        assertEquals("Renombrada", updated.getName());
        assertEquals("FREE", updated.getSubscriptionPlan());
        assertEquals(Boolean.FALSE, updated.getActive(), "active no es editable vía update");
        assertEquals(appKey, updated.getWhatsappAppKey(), "el vínculo WhatsApp no se toca");
        assertNotNull(updated.getUpdatedAt());
        assertEquals(1, repository.stored.size(), "no crea copias");
    }

    @Test
    @DisplayName("getPaginated devuelve PagedResult del dominio filtrando por keyword")
    void getPaginatedFiltersByKeyword() {
        service.create(draft("Cooperativa El Progreso"));
        service.create(draft("Asociación Las Flores"));
        service.create(draft("El Progreso II"));

        PagedResult<Community> result = service.getPaginated(new CommunityQuery("progreso"), PageParams.of(0, 10));

        assertEquals(2, result.totalElements());
        assertEquals(2, result.items().size());
        assertTrue(result.items().stream().allMatch(c -> c.getName().contains("Progreso")));
        assertEquals(0, result.page());
        assertEquals(10, result.size());
    }

    @Test
    @DisplayName("getPaginated con keyword vacío devuelve todo")
    void getPaginatedWithoutKeywordReturnsAll() {
        service.create(draft("A"));
        service.create(draft("B"));

        PagedResult<Community> result = service.getPaginated(new CommunityQuery(null), PageParams.of(0, 10));

        assertEquals(2, result.totalElements());
    }

    @Test
    @DisplayName("delete elimina la comunidad")
    void deleteRemovesCommunity() {
        Community saved = repository.save(draft("Borrable"));

        service.delete(saved.getKey().toString());

        assertTrue(repository.findById(saved.getKey()).isEmpty());
    }

    @Test
    @DisplayName("findByWhatsappAppKey solo devuelve comunidades activas")
    void findByWhatsappAppKeyOnlyActive() {
        UUID appKey = UUID.randomUUID();
        Community active = repository.save(draft("Activa"));
        active.setWhatsappAppKey(appKey);
        active.setActive(true);
        repository.save(active);

        Community inactive = repository.save(draft("Inactiva"));
        inactive.setWhatsappAppKey(appKey);
        inactive.setActive(false);
        repository.save(inactive);

        Optional<Community> found = service.findByWhatsappAppKey(appKey);

        assertTrue(found.isPresent());
        assertEquals("Activa", found.get().getName());
    }

    // ── PageParams ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("PageParams rechaza página negativa y tamaño < 1")
    void pageParamsRejectsInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> new PageParams(-1, 10));
        assertThrows(IllegalArgumentException.class, () -> new PageParams(0, 0));
        assertThrows(IllegalArgumentException.class, () -> new PageParams(0, -5));
    }

    @Test
    @DisplayName("PageParams limita el tamaño excesivo a MAX_SIZE")
    void pageParamsClampsOversizedSize() {
        PageParams params = new PageParams(0, 5_000);
        assertEquals(PageParams.MAX_SIZE, params.size());
    }

    @Test
    @DisplayName("PageParams.of convierte nulos a los valores por defecto")
    void pageParamsOfDefaults() {
        PageParams params = PageParams.of(null, null);
        assertEquals(0, params.page());
        assertEquals(PageParams.DEFAULT_SIZE, params.size());
    }

    @Test
    @DisplayName("PagedResult es inmutable: la lista no se puede alterar desde fuera")
    void pagedResultDefensivelyCopiesItems() {
        List<Community> mutable = new ArrayList<>(List.of(draft("X")));
        PagedResult<Community> result = new PagedResult<>(mutable, 0, 10, 1, 1);

        mutable.clear();

        assertEquals(1, result.items().size(), "el record copió la lista en el constructor");
        assertThrows(UnsupportedOperationException.class, () -> result.items().add(draft("Y")));
    }

    // ── Puerto fake en memoria ──────────────────────────────────────────────

    /** Implementación en memoria del puerto: sin Spring ni base de datos. */
    static class InMemoryCommunityRepository implements CommunityRepository {

        final Map<UUID, Community> stored = new java.util.LinkedHashMap<>();

        @Override
        public Community save(Community community) {
            if (community.getKey() == null) {
                community.setKey(UUID.randomUUID());
            }
            stored.put(community.getKey(), community);
            return community;
        }

        @Override
        public Optional<Community> findById(UUID key) {
            return Optional.ofNullable(stored.get(key));
        }

        @Override
        public List<Community> findAll() {
            return List.copyOf(stored.values());
        }

        @Override
        public PagedResult<Community> findAll(CommunityQuery query, PageParams page) {
            List<Community> filtered = stored.values().stream()
                    .filter(c -> matches(c, query.keyword()))
                    .collect(Collectors.toList());
            int from = Math.min(page.page() * page.size(), filtered.size());
            int to = Math.min(from + page.size(), filtered.size());
            List<Community> slice = filtered.subList(from, to);
            int totalPages = (int) Math.ceil((double) filtered.size() / page.size());
            return new PagedResult<>(slice, page.page(), page.size(), filtered.size(),
                    Math.max(totalPages, filtered.isEmpty() ? 1 : 0));
        }

        @Override
        public void deleteById(UUID key) {
            stored.remove(key);
        }

        @Override
        public Optional<Community> findByWhatsappAppKeyAndActiveTrue(UUID whatsappAppKey) {
            return stored.values().stream()
                    .filter(c -> Boolean.TRUE.equals(c.getActive()))
                    .filter(c -> whatsappAppKey.equals(c.getWhatsappAppKey()))
                    .findFirst();
        }

        private boolean matches(Community community, String keyword) {
            if (keyword == null || keyword.isBlank()) return true;
            String needle = keyword.toLowerCase();
            return contains(community.getName(), needle) || contains(community.getDescription(), needle);
        }

        private boolean contains(String value, String needle) {
            return value != null && value.toLowerCase().contains(needle);
        }
    }
}
