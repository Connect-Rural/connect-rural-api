package com.crdev.connect_rural_api.business.whatsapp.usecases;

import com.crdev.connect_rural_api.app.whatsapp.dto.request.GatewayEventDto;
import com.crdev.connect_rural_api.app.whatsapp.dto.request.GatewayMessageDto;
import com.crdev.connect_rural_api.app.whatsapp.dto.request.GatewayStatusDto;
import com.crdev.connect_rural_api.business.community.CommunityService;
import com.crdev.connect_rural_api.business.whatsapp.WhatsappGatewayService;
import com.crdev.connect_rural_api.data.community.CommunityEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessGatewayEventUseCase {

    private final WhatsappGatewayService gatewayService;
    private final CommunityService communityService;

    public void execute(GatewayEventDto event) {
        if (event == null || event.getEvent() == null) return;

        UUID appKey = UUID.fromString(event.getTenantKey());

        // Resolver la comunidad asociada a este tenant del gateway
        CommunityEntity community = communityService.findByWhatsappAppKey(appKey).orElse(null);

        if (community == null) {
            log.warn("[WhatsApp] Evento ignorado - no hay comunidad vinculada al tenant {}", appKey);
            return;
        }

        switch (event.getEvent()) {
            case "MESSAGE_RECEIVED" -> handleMessage(community, event.getMessage());
            case "MESSAGE_STATUS_UPDATED" -> handleStatus(event.getStatus());
            default -> log.warn("[WhatsApp] Evento desconocido recibido del gateway: {}", event.getEvent());
        }
    }

    private void handleMessage(CommunityEntity community, GatewayMessageDto message) {
        if (message == null) return;

        log.info("[WhatsApp] Mensaje recibido - comunidad: {}, de: {}, tipo: {}",
                community.getName(), message.getFrom(), message.getType());

        if ("text".equalsIgnoreCase(message.getType()) && message.getText() != null) {
            log.info("[WhatsApp] Texto recibido: {}", message.getText());

            // Punto de extensión: agregar aquí lógica de respuesta automática por comunidad.
            gatewayService.sendTextMessage(
                    community.getWhatsappAppKey(),
                    message.getFrom(),
                    "Bienvenido a " + community.getName() + ". ¿En qué podemos ayudarte?"
            );
        }
    }

    private void handleStatus(GatewayStatusDto status) {
        if (status == null) return;
        log.info("[WhatsApp] Estado actualizado - messageId: {}, estado: {}, destinatario: {}",
                status.getMessageId(), status.getStatus(), status.getRecipient());
    }
}
