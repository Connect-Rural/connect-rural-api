package com.crdev.connect_rural_api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class WhatsappGatewayConfig {

    @Value("${whatsapp.gateway.url}")
    private String gatewayUrl;

    @Value("${whatsapp.gateway.api-key}")
    private String apiKey;

    @Bean
    public RestClient whatsappGatewayClient() {
        return RestClient.builder()
                .baseUrl(gatewayUrl)
                .defaultHeader("X-API-Key", apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
