package com.crdev.connect_rural_api.business.whatsapp.usecases;

import com.crdev.connect_rural_api.business.community.CommunityService;
import com.crdev.connect_rural_api.business.resident.ResidentService;
import com.crdev.connect_rural_api.data.community.CommunityEntity;
import com.crdev.connect_rural_api.data.resident.ResidentEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotifyResidentUseCase {

    private final CommunityService communityService;
    private final ResidentService residentService;
    private final SendWhatsappMessageUseCase sendWhatsappMessageUseCase;

    /**
     * Envía un mensaje de texto a un residente por WhatsApp.
     * Requiere que la comunidad tenga whatsappAppKey configurado
     * y que el residente tenga phoneNumber registrado.
     */
    public void execute(String communityKey, String residentKey, String message) {
        CommunityEntity community = communityService.getBykey(communityKey);

        if (community.getWhatsappAppKey() == null) {
            throw new IllegalStateException(
                    "La comunidad no tiene WhatsApp configurado. " +
                    "Registre primero un tenant en POST /api/communities/{key}/whatsapp"
            );
        }

        ResidentEntity resident = residentService.getByKey(communityKey, residentKey);

        if (resident.getPhoneNumber() == null || resident.getPhoneNumber().isBlank()) {
            throw new IllegalStateException(
                    "El residente no tiene número de teléfono registrado"
            );
        }

        sendWhatsappMessageUseCase.execute(
                community.getWhatsappAppKey(),
                resident.getPhoneNumber(),
                message
        );

        log.info("[WhatsApp] Notificación enviada al residente {} de la comunidad {}",
                residentKey, community.getName());
    }
}
