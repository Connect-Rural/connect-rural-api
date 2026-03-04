package com.crdev.connect_rural_api.business.webhook;

import com.crdev.connect_rural_api.app.webhook.dto.request.WhatsappMessageDto;
import com.crdev.connect_rural_api.app.webhook.dto.request.WhatsappStatusDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WhatsappWebhookService {

    public void processMessage(String phoneNumberId, WhatsappMessageDto message) {
        log.info("[WhatsApp] Mensaje recibido - phoneNumberId: {}, de: {}, tipo: {}, id: {}",
                phoneNumberId, message.getFrom(), message.getType(), message.getId());

        if ("text".equals(message.getType()) && message.getText() != null) {
            log.info("[WhatsApp] Texto: {}", message.getText().getBody());
        }
    }

    public void processStatus(WhatsappStatusDto status) {
        log.info("[WhatsApp] Estado de mensaje - id: {}, estado: {}, destinatario: {}",
                status.getId(), status.getStatus(), status.getRecipientId());
    }
}
