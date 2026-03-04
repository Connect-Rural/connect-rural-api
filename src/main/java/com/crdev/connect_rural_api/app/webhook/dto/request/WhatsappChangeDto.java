package com.crdev.connect_rural_api.app.webhook.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WhatsappChangeDto {

    @JsonProperty("value")
    private WhatsappValueDto value;

    @JsonProperty("field")
    private String field;
}
