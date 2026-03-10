package com.crdev.connect_rural_api.business.whatsapp.usecases;

import com.crdev.connect_rural_api.app.whatsapp.dto.request.SendWhatsappMessageDto;
import com.crdev.connect_rural_api.business.whatsapp.WhatsappGatewayService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SendWhatsappMessageUseCase {

    private final WhatsappGatewayService gatewayService;

    public void execute(UUID appKey, String to, String text) {
        gatewayService.sendTextMessage(appKey, to, text);
    }

    public void execute(UUID appKey, SendWhatsappMessageDto dto) {
        gatewayService.send(appKey, dto);
    }
}
