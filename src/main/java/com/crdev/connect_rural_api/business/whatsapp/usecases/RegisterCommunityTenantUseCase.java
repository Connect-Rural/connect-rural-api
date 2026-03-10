package com.crdev.connect_rural_api.business.whatsapp.usecases;

import com.crdev.connect_rural_api.app.community.dto.request.RegisterWhatsappTenantDto;
import com.crdev.connect_rural_api.business.community.CommunityService;
import com.crdev.connect_rural_api.business.whatsapp.WhatsappGatewayService;
import com.crdev.connect_rural_api.data.community.CommunityEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterCommunityTenantUseCase {

    private final CommunityService communityService;
    private final WhatsappGatewayService gatewayService;

    @Value("${app.base-url}")
    private String appBaseUrl;

    /**
     * Registra la comunidad como tenant en el gateway y persiste el appKey resultante.
     * Falla si la comunidad ya tiene un tenant activo vinculado.
     *
     * @return appKey asignado por el gateway
     */
    public UUID execute(String communityKey, RegisterWhatsappTenantDto request) {
        CommunityEntity community = communityService.getBykey(communityKey);

        if (community.getWhatsappAppKey() != null) {
            throw new IllegalStateException(
                    "La comunidad ya tiene un tenant de WhatsApp vinculado: " + community.getWhatsappAppKey()
            );
        }

        String callbackUrl = appBaseUrl + "/api/whatsapp/events";

        UUID appKey = gatewayService.registerTenant(
                community.getName(),
                request.getPhoneNumberId(),
                request.getAccessToken(),
                callbackUrl,
                request.getAllowedMessageTypes()
        );

        community.setWhatsappAppKey(appKey);
        communityService.update(community);

        log.info("[WhatsApp] Comunidad '{}' vinculada al tenant {} del gateway", community.getName(), appKey);
        return appKey;
    }
}
