package com.anddev741.BitData.infrastructure.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.netty.http.server.HttpServer;

/**
 * Ensures Reactor Netty HTTP server metrics are bound to Micrometer.
 * This is needed because they are not emitted by default in some environments.
 */
@Configuration
@ConditionalOnClass({HttpServer.class, MeterRegistry.class})
public class NettyMetricsConfig {

    @Bean
    public WebServerFactoryCustomizer<NettyReactiveWebServerFactory> nettyMetricsCustomizer() {
        return factory -> factory.addServerCustomizers(httpServer -> httpServer.metrics(true, uri -> uri));
    }
}
