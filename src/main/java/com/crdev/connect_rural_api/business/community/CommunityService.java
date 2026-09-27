package com.crdev.connect_rural_api.business.community;

import com.crdev.connect_rural_api.business.shared.PageParams;
import com.crdev.connect_rural_api.business.shared.PagedResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Casos de uso del dominio comunidad.
 *
 * Trabaja únicamente con el modelo {@link Community}: no conoce entidades JPA
 * ni DTOs HTTP. El Controller (app/) traduce DTO → Community antes de llamar y
 * Community → DTO después de recibir.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CommunityService {

    private final CommunityRepository communityRepository;

    public Community create(Community draft) {
        log.info("Creating community: name={}", draft.getName());
        draft.setKey(null);
        draft.setCompletedConfiguration(false);
        draft.setActive(true);
        draft.setCreatedAt(null);
        draft.setUpdatedAt(null);
        return communityRepository.save(draft);
    }

    public List<Community> getList() {
        return communityRepository.findAll();
    }

    public Community getByKey(String key) {
        return find(key);
    }

    @Transactional(readOnly = true)
    public PagedResult<Community> getPaginated(CommunityQuery query, PageParams page) {
        return communityRepository.findAll(query, page);
    }

    @Transactional
    public Community update(String key, Community draft) {
        log.info("Updating community: key={}", key);
        Community existing = find(key);
        existing.updateFrom(draft);
        existing.setUpdatedAt(LocalDateTime.now());
        return communityRepository.save(existing);
    }

    @Transactional
    public void delete(String key) {
        log.info("Deleting community: key={}", key);
        find(key);
        communityRepository.deleteById(UUID.fromString(key));
    }

    private Community find(String key) {
        return communityRepository.findById(UUID.fromString(key))
                .orElseThrow(() -> new IllegalArgumentException("Community not found"));
    }

    /**
     * Comunidad activa vinculada a un tenant de WhatsApp (appKey del gateway).
     */
    public Optional<Community> findByWhatsappAppKey(UUID appKey) {
        return communityRepository.findByWhatsappAppKeyAndActiveTrue(appKey);
    }
}
