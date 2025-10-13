package com.anddev741.BitData.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;

@Configuration
public class WebSocketClientConfig {
    
    @SuppressWarnings("deprecation")
    @Bean
    public ReactorNettyWebSocketClient reactorNettyWebSocketClient() {
        ReactorNettyWebSocketClient client = new ReactorNettyWebSocketClient();
        client.setMaxFramePayloadLength(2 * 1024 * 1024); //2mb
        return client;
    }
}
