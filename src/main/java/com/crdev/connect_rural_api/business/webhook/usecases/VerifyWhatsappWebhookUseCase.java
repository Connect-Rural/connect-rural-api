package com.crdev.connect_rural_api.business.webhook.usecases;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class VerifyWhatsappWebhookUseCase {

    @Value("${whatsapp.webhook.verify-token}")
    private String configuredVerifyToken;

    public ResponseEntity<String> execute(String mode, String token, String challenge) {
        if ("subscribe".equals(mode) && configuredVerifyToken.equals(token)) {
            log.info("[WhatsApp] Webhook verificado exitosamente");
            return ResponseEntity.ok(challenge);
        }
        log.warn("[WhatsApp] Verificación de webhook fallida - modo: {}", mode);
        return ResponseEntity.status(403).body("Forbidden");
    }
}
