package com.crdev.connect_rural_api.business.webhook.usecases;

import com.crdev.connect_rural_api.app.webhook.dto.request.WhatsappChangeDto;
import com.crdev.connect_rural_api.app.webhook.dto.request.WhatsappEntryDto;
import com.crdev.connect_rural_api.app.webhook.dto.request.WhatsappMessageDto;
import com.crdev.connect_rural_api.app.webhook.dto.request.WhatsappStatusDto;
import com.crdev.connect_rural_api.app.webhook.dto.request.WhatsappValueDto;
import com.crdev.connect_rural_api.app.webhook.dto.request.WhatsappWebhookPayloadDto;
import com.crdev.connect_rural_api.business.webhook.WhatsappWebhookService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Slf4j
@Component
@AllArgsConstructor
public class ProcessWhatsappWebhookUseCase {

    private final WhatsappWebhookService whatsappWebhookService;

    public void execute(WhatsappWebhookPayloadDto payload) {
        if (!"whatsapp_business_account".equals(payload.getObject())) {
            log.warn("[WhatsApp] Payload ignorado - objeto desconocido: {}", payload.getObject());
            return;
        }

        if (CollectionUtils.isEmpty(payload.getEntry())) {
            return;
        }

        for (WhatsappEntryDto entry : payload.getEntry()) {
            if (CollectionUtils.isEmpty(entry.getChanges())) continue;

            for (WhatsappChangeDto change : entry.getChanges()) {
                if (!"messages".equals(change.getField())) continue;

                WhatsappValueDto value = change.getValue();
                if (value == null) continue;

                String phoneNumberId = value.getMetadata() != null
                        ? value.getMetadata().getPhoneNumberId()
                        : null;

                if (!CollectionUtils.isEmpty(value.getMessages())) {
                    for (WhatsappMessageDto message : value.getMessages()) {
                        whatsappWebhookService.processMessage(phoneNumberId, message);
                    }
                }

                if (!CollectionUtils.isEmpty(value.getStatuses())) {
                    for (WhatsappStatusDto status : value.getStatuses()) {
                        whatsappWebhookService.processStatus(status);
                    }
                }
            }
        }
    }
}
