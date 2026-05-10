package com.crdev.connect_rural_api.app.whatsapp;

import com.crdev.connect_rural_api.app.whatsapp.dto.GatewayEventRequest;
import com.crdev.connect_rural_api.business.whatsapp.WhatsappService;
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
public class WhatsappCallbackController {

    private final WhatsappService whatsappService;

    @PostMapping
    public ResponseEntity<Void> receiveEvent(@RequestBody GatewayEventRequest event) {
        try {
            whatsappService.processEvent(event);
        } catch (Exception e) {
            log.error("[WhatsApp] Error al procesar evento del gateway: {}", e.getMessage());
        }
        return ResponseEntity.ok().build();
    }
}
