package com.crdev.connect_rural_api.business.community;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Modelo de dominio de una comunidad.
 *
 * Es el tipo que conoce business/: las interfaces puerto devuelven y reciben
 * {@code Community}, nunca {@code CommunityEntity}. El mapeo Entity ↔ Community
 * vive en data/ ({@code CommunityEntityMapper}) y el mapeo Community ↔ DTO HTTP
 * vive en app/ ({@code CommunityMapper}).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Community {

    private UUID key;
    private String name;
    private String description;
    private String logoUrl;
    private String address;
    private String state;
    private String municipality;
    private String postalCode;
    private String subscriptionPlan;
    private Boolean completedConfiguration;
    private UUID whatsappAppKey;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Aplica sobre esta comunidad los campos editables de un borrador.
     * Solo los campos que el usuario puede modificar: nombre, descripción,
     * logo y plan. El resto (active, completedConfiguration, whatsappAppKey,
     * fechas) se gestiona internamente.
     */
    public void updateFrom(Community draft) {
        if (draft == null) return;
        this.name = draft.name;
        this.description = draft.description;
        this.logoUrl = draft.logoUrl;
        this.subscriptionPlan = draft.subscriptionPlan;
    }
}
