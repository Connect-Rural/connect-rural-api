package com.crdev.connect_rural_api.service.resident.exception;

import com.crdev.connect_rural_api.service.shared.DomainException;
import java.util.UUID;

public class ResidentNotInCommunityException extends DomainException {

    private final UUID residentKey;
    private final UUID communityKey;

    public ResidentNotInCommunityException(UUID residentKey, UUID communityKey) {
        super("Resident %s does not belong to community %s".formatted(residentKey, communityKey));
        this.residentKey = residentKey;
        this.communityKey = communityKey;
    }

    public UUID getResidentKey() { return residentKey; }
    public UUID getCommunityKey() { return communityKey; }
}