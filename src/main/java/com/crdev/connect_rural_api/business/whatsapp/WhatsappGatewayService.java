package com.crdev.connect_rural_api.business.whatsapp;

import com.crdev.connect_rural_api.app.whatsapp.dto.request.SendWhatsappMessageDto;
import com.crdev.connect_rural_api.business.whatsapp.dto.GatewayTenantResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WhatsappGatewayService {

    private final RestClient whatsappGatewayClient;

    /**
     * Envía un mensaje de texto simple usando el whatsapp-gateway.
     *
     * @param appKey UUID del tenant en el gateway (whatsappAppKey de la comunidad)
     */
    public void sendTextMessage(UUID appKey, String to, String text) {
        send(appKey, SendWhatsappMessageDto.builder()
                .to(to)
                .type("TEXT")
                .text(text)
                .build());
    }

    /**
     * Envía cualquier tipo de mensaje soportado por el gateway.
     *
     * @param appKey UUID del tenant en el gateway (whatsappAppKey de la comunidad)
     */
    public void send(UUID appKey, SendWhatsappMessageDto dto) {
        try {
            whatsappGatewayClient.post()
                    .uri("/api/messages/send?appKey={appKey}", appKey)
                    .body(dto)
                    .retrieve()
                    .toBodilessEntity();

            log.info("[WhatsApp] Mensaje {} enviado a {} vía gateway", dto.getType(), dto.getTo());
        } catch (Exception e) {
            log.error("[WhatsApp] Error al enviar mensaje a {} vía gateway: {}", dto.getTo(), e.getMessage());
            throw new RuntimeException("Error al enviar mensaje vía whatsapp-gateway: " + e.getMessage(), e);
        }
    }

    /**
     * Registra una nueva comunidad como tenant en el gateway.
     * Retorna el appKey (tenantKey) asignado por el gateway.
     *
     * @param name               nombre de la comunidad
     * @param phoneNumberId      phone_number_id de la cuenta de WhatsApp Business
     * @param accessToken        access_token de la cuenta de WhatsApp Business
     * @param callbackUrl        URL donde el gateway enviará los eventos (este servicio)
     * @param allowedMessageTypes tipos de mensaje permitidos; si es null o vacío se usa ["TEXT"]
     */
    public UUID registerTenant(String name, String phoneNumberId, String accessToken,
                               String callbackUrl, List<String> allowedMessageTypes) {
        List<String> types = (allowedMessageTypes == null || allowedMessageTypes.isEmpty())
                ? List.of("TEXT")
                : allowedMessageTypes;

        Map<String, Object> body = Map.of(
                "name", name,
                "phoneNumberId", phoneNumberId,
                "accessToken", accessToken,
                "callbackUrl", callbackUrl,
                "allowedMessageTypes", types
        );

        try {
            GatewayTenantResponse response = whatsappGatewayClient.post()
                    .uri("/api/tenants")
                    .body(body)
                    .retrieve()
                    .body(GatewayTenantResponse.class);

            if (response == null || response.getAppKey() == null) {
                throw new RuntimeException("El gateway no retornó appKey al registrar el tenant");
            }

            log.info("[WhatsApp] Tenant registrado en gateway - nombre: {}, appKey: {}", name, response.getAppKey());
            return response.getAppKey();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("[WhatsApp] Error al registrar tenant '{}' en el gateway: {}", name, e.getMessage());
            throw new RuntimeException("Error al registrar tenant en whatsapp-gateway: " + e.getMessage(), e);
        }
    }

    /**
     * Elimina el tenant del gateway y desvincula la comunidad.
     *
     * @param appKey tenantKey asignado por el gateway
     */
    public void deleteTenant(UUID appKey) {
        try {
            whatsappGatewayClient.delete()
                    .uri("/api/tenants/{appKey}", appKey)
                    .retrieve()
                    .toBodilessEntity();

            log.info("[WhatsApp] Tenant eliminado del gateway - appKey: {}", appKey);
        } catch (Exception e) {
            log.error("[WhatsApp] Error al eliminar tenant {} del gateway: {}", appKey, e.getMessage());
            throw new RuntimeException("Error al eliminar tenant en whatsapp-gateway: " + e.getMessage(), e);
        }
    }
}
