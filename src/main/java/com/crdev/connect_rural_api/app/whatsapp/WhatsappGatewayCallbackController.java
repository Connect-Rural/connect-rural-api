package com.crdev.connect_rural_api.app.whatsapp;

import com.crdev.connect_rural_api.app.whatsapp.dto.request.GatewayEventDto;
import com.crdev.connect_rural_api.business.whatsapp.usecases.ProcessGatewayEventUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/whatsapp/events")
@RequiredArgsConstructor
public class WhatsappGatewayCallbackController {

    private final ProcessGatewayEventUseCase processGatewayEventUseCase;

    /**
     * El whatsapp-gateway llama a este endpoint con eventos normalizados.
     * Configura esta URL como callbackUrl del tenant en el gateway.
     * Siempre responde 200 OK para evitar reintentos del gateway.
     */
    @PostMapping
    public ResponseEntity<Void> receiveEvent(@RequestBody GatewayEventDto event) {
        try {
            processGatewayEventUseCase.execute(event);
        } catch (Exception e) {
            log.error("[WhatsApp] Error al procesar evento del gateway: {}", e.getMessage());
        }
        return ResponseEntity.ok().build();
    }
}
