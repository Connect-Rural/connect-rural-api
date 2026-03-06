package com.crdev.connect_rural_api.business.whatsapp.usecases;

import com.crdev.connect_rural_api.app.whatsapp.dto.request.SendWhatsappMessageDto;
import com.crdev.connect_rural_api.business.whatsapp.WhatsappGatewayService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SendWhatsappMessageUseCase {

    private final WhatsappGatewayService gatewayService;

    public void execute(String to, String text) {
        gatewayService.sendTextMessage(to, text);
    }

    public void execute(SendWhatsappMessageDto dto) {
        gatewayService.send(dto);
    }
}
