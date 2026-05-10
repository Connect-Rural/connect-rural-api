package com.crdev.connect_rural_api.business.whatsapp;

import com.crdev.connect_rural_api.app.community.dto.RegisterWhatsappTenantRequest;
import com.crdev.connect_rural_api.app.whatsapp.dto.GatewayEventRequest;
import com.crdev.connect_rural_api.app.whatsapp.dto.GatewayMessageRequest;
import com.crdev.connect_rural_api.app.whatsapp.dto.GatewayStatusRequest;
import com.crdev.connect_rural_api.business.community.CommunityRepository;
import com.crdev.connect_rural_api.business.resident.ResidentRepository;
import com.crdev.connect_rural_api.data.community.CommunityEntity;
import com.crdev.connect_rural_api.data.resident.ResidentEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WhatsappService {

    private final WhatsappGateway gateway;
    private final CommunityRepository communityRepository;
    private final ResidentRepository residentRepository;

    @Value("${app.base-url}")
    private String appBaseUrl;

    public void processEvent(GatewayEventRequest event) {
        if (event == null || event.getEvent() == null) return;
        UUID appKey = UUID.fromString(event.getTenantKey());
        CommunityEntity community = communityRepository.findByWhatsappAppKeyAndActiveTrue(appKey).orElse(null);
        if (community == null) {
            log.warn("[WhatsApp] Evento ignorado - no hay comunidad vinculada al tenant {}", appKey);
            return;
        }
        switch (event.getEvent()) {
            case "MESSAGE_RECEIVED" -> handleMessage(community, event.getMessage());
            case "MESSAGE_STATUS_UPDATED" -> handleStatus(event.getStatus());
            default -> log.warn("[WhatsApp] Evento desconocido: {}", event.getEvent());
        }
    }

    public UUID registerTenant(String communityKey, RegisterWhatsappTenantRequest request) {
        CommunityEntity community = findCommunity(communityKey);
        if (community.getWhatsappAppKey() != null) {
            throw new IllegalStateException(
                    "La comunidad ya tiene un tenant de WhatsApp vinculado: " + community.getWhatsappAppKey());
        }
        String callbackUrl = appBaseUrl + "/api/whatsapp/events";
        UUID appKey = gateway.registerTenant(community.getName(), request.getPhoneNumberId(),
                request.getAccessToken(), callbackUrl, request.getAllowedMessageTypes());
        community.setWhatsappAppKey(appKey);
        communityRepository.save(community);
        log.info("[WhatsApp] Comunidad '{}' vinculada al tenant {} del gateway", community.getName(), appKey);
        return appKey;
    }

    public void unlinkTenant(String communityKey) {
        CommunityEntity community = findCommunity(communityKey);
        if (community.getWhatsappAppKey() == null) {
            throw new IllegalStateException("La comunidad no tiene ningún tenant de WhatsApp vinculado");
        }
        gateway.deleteTenant(community.getWhatsappAppKey());
        community.setWhatsappAppKey(null);
        communityRepository.save(community);
        log.info("[WhatsApp] Comunidad '{}' desvinculada del gateway", community.getName());
    }

    public void notifyResident(String communityKey, String residentKey, String message) {
        CommunityEntity community = findCommunity(communityKey);
        if (community.getWhatsappAppKey() == null) {
            throw new IllegalStateException("La comunidad no tiene WhatsApp configurado");
        }
        ResidentEntity resident = residentRepository.findByCommunityKeyAndKey(
                UUID.fromString(communityKey), UUID.fromString(residentKey))
                .orElseThrow(() -> new IllegalArgumentException("Resident not found"));
        if (resident.getPhoneNumber() == null || resident.getPhoneNumber().isBlank()) {
            throw new IllegalStateException("El residente no tiene número de teléfono registrado");
        }
        gateway.sendTextMessage(community.getWhatsappAppKey(), resident.getPhoneNumber(), message);
        log.info("[WhatsApp] Notificación enviada al residente {} de la comunidad {}", residentKey, community.getName());
    }

    private void handleMessage(CommunityEntity community, GatewayMessageRequest message) {
        if (message == null) return;
        log.info("[WhatsApp] Mensaje recibido - comunidad: {}, de: {}, tipo: {}",
                community.getName(), message.getFrom(), message.getType());
        if ("text".equalsIgnoreCase(message.getType()) && message.getText() != null) {
            gateway.sendTextMessage(community.getWhatsappAppKey(), message.getFrom(),
                    "Bienvenido a " + community.getName() + ". ¿En qué podemos ayudarte?");
        }
    }

    private void handleStatus(GatewayStatusRequest status) {
        if (status == null) return;
        log.info("[WhatsApp] Estado actualizado - messageId: {}, estado: {}, destinatario: {}",
                status.getMessageId(), status.getStatus(), status.getRecipient());
    }

    private CommunityEntity findCommunity(String communityKey) {
        return communityRepository.findById(UUID.fromString(communityKey))
                .orElseThrow(() -> new IllegalArgumentException("Community not found"));
    }
}
