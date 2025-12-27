package com.anddev741.BitData.infrastructure.messaging;

import com.anddev741.BitData.application.service.AdvancedAnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
public class AdvancedProcessor {

    @Autowired
    AdvancedAnalyticsService advancedAnalyticsService;

    @Bean
    public Consumer<String> advancedProcess() {
        return analyticId -> {
            advancedAnalyticsService.aggregateAdvancedAnalytics(analyticId);
        };
    }

}
