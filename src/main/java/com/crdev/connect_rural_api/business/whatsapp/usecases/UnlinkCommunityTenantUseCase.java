package com.crdev.connect_rural_api.business.whatsapp.usecases;

import com.crdev.connect_rural_api.business.community.CommunityService;
import com.crdev.connect_rural_api.business.whatsapp.WhatsappGatewayService;
import com.crdev.connect_rural_api.data.community.CommunityEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UnlinkCommunityTenantUseCase {

    private final CommunityService communityService;
    private final WhatsappGatewayService gatewayService;

    /**
     * Elimina el tenant del gateway y limpia el appKey de la comunidad.
     * Falla si la comunidad no tiene ningún tenant vinculado.
     */
    public void execute(String communityKey) {
        CommunityEntity community = communityService.getBykey(communityKey);

        if (community.getWhatsappAppKey() == null) {
            throw new IllegalStateException(
                    "La comunidad no tiene ningún tenant de WhatsApp vinculado"
            );
        }

        gatewayService.deleteTenant(community.getWhatsappAppKey());

        community.setWhatsappAppKey(null);
        communityService.update(community);

        log.info("[WhatsApp] Comunidad '{}' desvinculada del gateway", community.getName());
    }
}
