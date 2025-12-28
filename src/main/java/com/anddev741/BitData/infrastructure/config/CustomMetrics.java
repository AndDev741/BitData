package com.anddev741.BitData.infrastructure.config;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

@Component
public class CustomMetrics {
    
    private final Counter unconfirmedTransactionsReceived;
    private final Counter failedTransactionsCounter;
    private final Counter connectionRetries;
    private final Counter rawTransactionSave;
    private final Counter statisticsTransactionSave;
    private final Counter advancedStatisticSaved;
    private final Counter advancedStatisticFailed;

    public CustomMetrics(MeterRegistry registry) {
        this.unconfirmedTransactionsReceived = Counter
            .builder("bitdata_ws_unconfirmed_transactions_total")
            .description("Total of transactions not confirmed since the build of application")
            .register(registry);

        this.failedTransactionsCounter = Counter
            .builder("bitdata_ws_failed_transactions_total")
            .description("Total of transaction with failure in the process")
            .register(registry);

        this.connectionRetries = Counter
            .builder("bitdata_ws_retries")
            .description("Total of retries of the webSocket")
            .register(registry);

        this.rawTransactionSave = Counter
                .builder("bitdata_raw_transactions_save")
                .description("Total os raw transactions saved in the database")
                .register(registry);

        this.statisticsTransactionSave = Counter
                .builder("bitdata_statistics_persisted")
                .description("Total of statistics persisted in the database")
                .register(registry);

        this.advancedStatisticSaved = Counter
                .builder("bitdata_advanced_statistics_persisted")
                .description("Total of Advanced statistics persisted in the database")
                .register(registry);

        this.advancedStatisticFailed = Counter
                .builder("bitdata_advanced_statistics_failed")
                .description("Total of Advanced statistics that failed in the proccess")
                .register(registry);
    }

    public void incrementUnconfirmedTransaction() {
        unconfirmedTransactionsReceived.increment();
    }

    public void incrementFailed() {
        failedTransactionsCounter.increment();
    }

    public void incrementWebSocketRetries(){
        connectionRetries.increment();
    }

    public void incrementPersistedUnconfirmedTransactions() {
        rawTransactionSave.increment();
    }

    public void incrementStatisticsPersisted() {
        statisticsTransactionSave.increment();
    }

    public void incrementAdvancedStatisticsPersisted() {
        advancedStatisticSaved.increment();
    }

    public void incrementAdvancedStatisticsFailed() {
        advancedStatisticFailed.increment();
    }
}
