package com.crdev.connect_rural_api.business.whatsapp;

import com.crdev.connect_rural_api.app.whatsapp.dto.SendWhatsappMessageRequest;

import java.util.List;
import java.util.UUID;

public interface WhatsappGateway {
    void sendTextMessage(UUID appKey, String to, String text);
    void send(UUID appKey, SendWhatsappMessageRequest request);
    UUID registerTenant(String name, String phoneNumberId, String accessToken,
                        String callbackUrl, List<String> allowedMessageTypes);
    void deleteTenant(UUID appKey);
}
