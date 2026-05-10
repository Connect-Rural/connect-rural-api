package com.crdev.connect_rural_api.app.whatsapp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class SendWhatsappMessageRequest {

    @NotBlank
    private String to;

    @NotBlank
    private String type;

    private String text;
    private String mediaUrl;
    private String caption;
    private String filename;
    private String templateName;
    private String languageCode;
    private List<Map<String, Object>> templateComponents;
    private Map<String, Object> interactive;
    private String reactionMessageId;
    private String emoji;
}
