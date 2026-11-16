package com.ziyad.courselens.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class GeminiConfig {

    @Bean
    public RestClient restClient(RestClient.Builder builder,
                                    @Value("${gemini.api.url}") String apiUrl,
                                    @Value("${gemini.api.key}") String apiKey){
        return builder
                .baseUrl(apiUrl)
                .defaultHeader("x-goog-api-key",apiKey)
                .build();
    }
}
