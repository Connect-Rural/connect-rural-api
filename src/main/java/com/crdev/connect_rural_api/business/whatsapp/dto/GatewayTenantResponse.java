package com.crdev.connect_rural_api.business.whatsapp.dto;

import lombok.Data;

import java.util.UUID;

/**
 * Subconjunto de la respuesta TenantResponse del whatsapp-gateway.
 * Solo se usa para extraer el appKey tras registrar un tenant.
 */
@Data
public class GatewayTenantResponse {
    private UUID appKey;
}
