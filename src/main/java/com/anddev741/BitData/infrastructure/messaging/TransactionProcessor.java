package com.anddev741.BitData.infrastructure.messaging;

import java.util.function.Consumer;

import com.anddev741.BitData.application.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.anddev741.BitData.domain.model.UnconfirmedTransaction.UnconfirmedTransaction;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class TransactionProcessor {

    @Autowired
    AnalyticsService analyticsService;

    @Bean
    public Consumer<UnconfirmedTransaction> processTransactions() {
        return transaction -> {
            analyticsService.generateStatisticFromUnconfirmedTransaction(transaction);
        };
    }
}
