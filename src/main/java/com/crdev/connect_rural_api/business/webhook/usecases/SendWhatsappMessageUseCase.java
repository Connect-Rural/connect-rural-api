package com.crdev.connect_rural_api.business.webhook.usecases;

import com.crdev.connect_rural_api.app.webhook.dto.request.SendWhatsappMessageDto;
import com.crdev.connect_rural_api.business.webhook.WhatsappMessageSenderService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class SendWhatsappMessageUseCase {

    private final WhatsappMessageSenderService senderService;

    public void execute(SendWhatsappMessageDto dto) {
        senderService.sendTextMessage(dto.getTo(), dto.getMessage());
    }

    public void execute(String phoneNumberId, SendWhatsappMessageDto dto) {
        senderService.sendTextMessage(phoneNumberId, dto.getTo(), dto.getMessage());
    }
}
