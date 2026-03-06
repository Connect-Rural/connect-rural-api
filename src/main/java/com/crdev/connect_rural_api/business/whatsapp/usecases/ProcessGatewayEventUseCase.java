package com.crdev.connect_rural_api.business.whatsapp.usecases;

import com.crdev.connect_rural_api.app.whatsapp.dto.request.GatewayEventDto;
import com.crdev.connect_rural_api.app.whatsapp.dto.request.GatewayMessageDto;
import com.crdev.connect_rural_api.app.whatsapp.dto.request.GatewayStatusDto;
import com.crdev.connect_rural_api.business.whatsapp.WhatsappGatewayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessGatewayEventUseCase {

    private final WhatsappGatewayService gatewayService;

    public void execute(GatewayEventDto event) {
        if (event == null || event.getEvent() == null) return;

        switch (event.getEvent()) {
            case "MESSAGE_RECEIVED" -> handleMessage(event.getMessage());
            case "MESSAGE_STATUS_UPDATED" -> handleStatus(event.getStatus());
            default -> log.warn("[WhatsApp] Evento desconocido recibido del gateway: {}", event.getEvent());
        }
    }

    private void handleMessage(GatewayMessageDto message) {
        if (message == null) return;

        log.info("[WhatsApp] Mensaje recibido - de: {}, tipo: {}", message.getFrom(), message.getType());

        if ("text".equalsIgnoreCase(message.getType()) && message.getText() != null) {
            log.info("[WhatsApp] Texto: {}", message.getText());

            // Punto de extension: implementar logica de respuesta automatica aqui.
            gatewayService.sendTextMessage(
                    message.getFrom(),
                    "Bienvenido a CR-CONNECT, esto es una respuesta automatica."
            );
        }
    }

    private void handleStatus(GatewayStatusDto status) {
        if (status == null) return;
        log.info("[WhatsApp] Estado - messageId: {}, estado: {}, destinatario: {}",
                status.getMessageId(), status.getStatus(), status.getRecipient());
    }
}
