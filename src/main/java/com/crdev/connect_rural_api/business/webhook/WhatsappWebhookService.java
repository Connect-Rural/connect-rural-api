package com.crdev.connect_rural_api.business.webhook;

import com.crdev.connect_rural_api.app.webhook.dto.request.WhatsappMessageDto;
import com.crdev.connect_rural_api.app.webhook.dto.request.WhatsappStatusDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WhatsappWebhookService {

    private final WhatsappMessageSenderService senderService;

    public void processMessage(String phoneNumberId, WhatsappMessageDto message) {
        log.info("[WhatsApp] Mensaje recibido - phoneNumberId: {}, de: {}, tipo: {}, id: {}",
                phoneNumberId, message.getFrom(), message.getType(), message.getId());

        if ("text".equals(message.getType()) && message.getText() != null) {
            String body = message.getText().getBody();
            log.info("[WhatsApp] Texto: {}", body);

            // Punto de extensión: implementar lógica de respuesta automática aquí.
             senderService.sendTextMessage(phoneNumberId, message.getFrom(), "Bienvenido a CR-CONNECT, esto es una respuesta automatica  " + body);
        }
    }

    public void processStatus(WhatsappStatusDto status) {
        log.info("[WhatsApp] Estado de mensaje - id: {}, estado: {}, destinatario: {}",
                status.getId(), status.getStatus(), status.getRecipientId());
    }
}
