package com.crdev.connect_rural_api.business.whatsapp;

import com.crdev.connect_rural_api.app.whatsapp.dto.request.SendWhatsappMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class WhatsappGatewayService {

    private final RestClient whatsappGatewayClient;

    /**
     * Envía un mensaje de texto simple usando el whatsapp-gateway.
     */
    public void sendTextMessage(String to, String text) {
        send(SendWhatsappMessageDto.builder()
                .to(to)
                .type("TEXT")
                .text(text)
                .build());
    }

    /**
     * Envía cualquier tipo de mensaje soportado por el gateway.
     */
    public void send(SendWhatsappMessageDto dto) {
        try {
            whatsappGatewayClient.post()
                    .uri("/api/messages/send")
                    .body(dto)
                    .retrieve()
                    .toBodilessEntity();

            log.info("[WhatsApp] Mensaje {} enviado a {} vía gateway", dto.getType(), dto.getTo());
        } catch (Exception e) {
            log.error("[WhatsApp] Error al enviar mensaje a {} vía gateway: {}", dto.getTo(), e.getMessage());
            throw new RuntimeException("Error al enviar mensaje vía whatsapp-gateway: " + e.getMessage(), e);
        }
    }
}
