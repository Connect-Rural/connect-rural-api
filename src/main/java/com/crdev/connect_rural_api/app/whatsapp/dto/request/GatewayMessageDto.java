package com.crdev.connect_rural_api.app.whatsapp.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class GatewayMessageDto {

    @JsonProperty("id")
    private String id;

    @JsonProperty("from")
    private String from;

    @JsonProperty("type")
    private String type;

    @JsonProperty("text")
    private String text;

    @JsonProperty("mediaUrl")
    private String mediaUrl;

    @JsonProperty("caption")
    private String caption;

    @JsonProperty("filename")
    private String filename;

    @JsonProperty("templateName")
    private String templateName;

    @JsonProperty("interactive")
    private Map<String, Object> interactive;

    @JsonProperty("reactionEmoji")
    private String reactionEmoji;

    @JsonProperty("reactionMessageId")
    private String reactionMessageId;

    @JsonProperty("receivedAt")
    private String receivedAt;
}
