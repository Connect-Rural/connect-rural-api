package com.crdev.connect_rural_api.app.whatsapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class SendWhatsappMessageDto {

    @NotBlank
    private String to;

    @NotBlank
    private String type;

    // TEXT
    private String text;

    // IMAGE, VIDEO, DOCUMENT, AUDIO
    private String mediaUrl;
    private String caption;
    private String filename;

    // TEMPLATE
    private String templateName;
    private String languageCode;
    private List<Map<String, Object>> templateComponents;

    // INTERACTIVE
    private Map<String, Object> interactive;

    // REACTION
    private String reactionMessageId;
    private String emoji;
}
