package com.crdev.connect_rural_api.business.webhook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WhatsappMessageSenderService {

    private final RestClient whatsappRestClient;

    @Value("${whatsapp.api.version}")
    private String apiVersion;

    @Value("${whatsapp.api.phone-number-id}")
    private String defaultPhoneNumberId;

    public void sendTextMessage(String to, String text) {
        sendTextMessage(defaultPhoneNumberId, to, text);
    }

    // México: WhatsApp envía el 'from' con formato legacy 521XXXXXXXXXX (13 dígitos).
    // La API de envío requiere 52XXXXXXXXXX (12 dígitos). Se elimina el '1' intermedio.
    private String normalizePhoneNumber(String phone) {
        if (phone != null && phone.startsWith("521") && phone.length() == 13) {
            return "52" + phone.substring(3);
        }
        return phone;
    }

    public void sendTextMessage(String phoneNumberId, String to, String text) {
        String url = "/" + apiVersion + "/" + phoneNumberId + "/messages";
        to = normalizePhoneNumber(to);

        Map<String, Object> body = Map.of(
                "messaging_product", "whatsapp",
                "recipient_type", "individual",
                "to", to,
                "type", "text",
                "text", Map.of("preview_url", false, "body", text)
        );

        try {
            whatsappRestClient.post()
                    .uri(url)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();

            log.info("[WhatsApp] Mensaje enviado a {} desde phoneNumberId {}", to, phoneNumberId);
        } catch (Exception e) {
            log.error("[WhatsApp] Error al enviar mensaje a {}: {}", to, e.getMessage());
            throw new RuntimeException("Error al enviar mensaje de WhatsApp", e);
        }
    }
}
