package com.anddev741.BitData.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WalletClientConfig {

    @Bean
    public WebClient walletWebClient(
            @Value("${wallet.api.base-url:https://blockchain.info}") String baseUrl,
            WebClient.Builder webClientBuilder) {
        return webClientBuilder.baseUrl(baseUrl).build();
    }
}
