package com.crdev.connect_rural_api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class WhatsappApiConfig {

    @Value("${whatsapp.api.base-url}")
    private String baseUrl;

    @Value("${whatsapp.api.access-token}")
    private String accessToken;

    @Bean
    public RestClient whatsappRestClient() {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + accessToken)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
