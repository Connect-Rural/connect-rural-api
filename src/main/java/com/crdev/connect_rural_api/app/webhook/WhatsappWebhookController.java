package com.crdev.connect_rural_api.app.webhook;

import com.crdev.connect_rural_api.app.webhook.dto.request.SendWhatsappMessageDto;
import com.crdev.connect_rural_api.app.webhook.dto.request.WhatsappWebhookPayloadDto;
import com.crdev.connect_rural_api.business.webhook.usecases.ProcessWhatsappWebhookUseCase;
import com.crdev.connect_rural_api.business.webhook.usecases.SendWhatsappMessageUseCase;
import com.crdev.connect_rural_api.business.webhook.usecases.VerifyWhatsappWebhookUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/webhook/whatsapp")
@RequiredArgsConstructor
public class WhatsappWebhookController {

    private final VerifyWhatsappWebhookUseCase verifyWebhookUseCase;
    private final ProcessWhatsappWebhookUseCase processWebhookUseCase;
    private final SendWhatsappMessageUseCase sendMessageUseCase;

    /**
     * Meta llama a este endpoint para verificar el webhook.
     * Requiere que hub.verify_token coincida con whatsapp.webhook.verify-token en application.properties.
     */
    @GetMapping
    public ResponseEntity<String> verify(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String verifyToken,
            @RequestParam("hub.challenge") String challenge) {
        return verifyWebhookUseCase.execute(mode, verifyToken, challenge);
    }

    /**
     * Meta envía los eventos de mensajes/estados a este endpoint.
     * Siempre responder con 200 OK para evitar reintentos de Meta.
     */
    @PostMapping
    public ResponseEntity<Void> receive(@RequestBody WhatsappWebhookPayloadDto payload) {
        processWebhookUseCase.execute(payload);
        return ResponseEntity.ok().build();
    }

    /**
     * Envía un mensaje de texto a un número de WhatsApp.
     * Body: { "to": "+521234567890", "message": "Hola!" }
     */
    @PostMapping("/send")
    public ResponseEntity<Void> send(@Valid @RequestBody SendWhatsappMessageDto dto) {
        sendMessageUseCase.execute(dto);
        return ResponseEntity.ok().build();
    }
}
