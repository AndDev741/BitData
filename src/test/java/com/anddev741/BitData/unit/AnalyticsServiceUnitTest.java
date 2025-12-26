package com.anddev741.BitData.unit;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.stream.function.StreamBridge;

import com.anddev741.BitData.application.service.AnalyticsService;
import com.anddev741.BitData.domain.model.TransactionStatistics.TransactionStatistics;
import com.anddev741.BitData.domain.model.UnconfirmedTransaction.Transaction;
import com.anddev741.BitData.domain.model.UnconfirmedTransaction.UnconfirmedTransaction;
import com.anddev741.BitData.infrastructure.config.CustomMetrics;
import com.anddev741.BitData.infrastructure.persistence.TransactionStatisticsRepository;
import com.anddev741.BitData.infrastructure.persistence.UnconfirmedTransactionRepository;
import com.anddev741.BitData.utils.ValidTransactionStatistics;
import com.anddev741.BitData.utils.ValidUnconfirmedTransaction;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class AnalyticsServiceUnitTest {

    @Mock
    private UnconfirmedTransactionRepository unconfirmedTransactionRepository;

    @Mock
    private TransactionStatisticsRepository transactionStatisticsRepository;

    @Mock
    private CustomMetrics customMetrics;

    @Mock
    private StreamBridge streamBridge;

    private AnalyticsService service;

    UnconfirmedTransaction uc = new UnconfirmedTransaction();

    TransactionStatistics tStatistics = new TransactionStatistics();

    @BeforeEach
    void setup() throws JsonMappingException, JsonProcessingException {
        service = new AnalyticsService(
                unconfirmedTransactionRepository,
                transactionStatisticsRepository,
                customMetrics,
                streamBridge);

        uc = ValidUnconfirmedTransaction.getValidUnconfirmedTransaction();
        tStatistics = ValidTransactionStatistics.getValidTransactionStatistics(); //It's using the default unconfirmed transaction
    }

    @Nested
    class PersistRawUnconfirmedTransaction {
        @Test
        public void shouldIncreaseNumberOfPersistedUnconfirmedTransactionAndSave() {
            // Arrange
            when(unconfirmedTransactionRepository.save(any(UnconfirmedTransaction.class)))
                    .thenReturn(Mono.just(uc));

            // Act
            Mono<Void> result = service.persistUnconfirmedTransaction(uc);

            // Assert
            StepVerifier.create(result)
                    .verifyComplete();

            verify(customMetrics, times(1)).incrementPersistedUnconfirmedTransactions();
            verify(unconfirmedTransactionRepository, times(1)).save(uc);
        }

        @Test
        void shouldTrowErrorWhenErrorOnPersistUnconfirmedTransaction() {
            // Arrange
            when(unconfirmedTransactionRepository.save(any()))
                    .thenReturn(Mono.error(new RuntimeException("Database failure")));

            //Act
            Mono<Void> result = service.persistUnconfirmedTransaction(new UnconfirmedTransaction());

            StepVerifier.create(result)
                    .expectError(RuntimeException.class)
                    .verify();

            // Assert
            verify(customMetrics, never()).incrementPersistedUnconfirmedTransactions();
        }
    }

    @Nested
    class CreateMetadataAndStatistics {
        
        @Test
        void shouldCreateMetadataAndStatisticsSuccessfully(){
            //Arrange
            Transaction transaction = uc.getX();
            //Act
            TransactionStatistics ts = AnalyticsService.createMetadataAndStatistics(uc);

            //Assert
            assertEquals(transaction.getHash(), ts.getHash());
            assertEquals(584, ts.getFee());
            assertEquals(196, ts.getSize());
            assertEquals(6371870, ts.getSenderList().get(0).getValueSent());
            assertEquals(101907, ts.getReceiverList().get(0).getValueReceived());
        }

        @Test
        void shouldPersistStatistics() {
            //Arrange
            when(unconfirmedTransactionRepository.save(any(UnconfirmedTransaction.class)))
                    .thenReturn(Mono.just(uc));
            when(transactionStatisticsRepository.save(any(TransactionStatistics.class)))
                    .thenReturn(Mono.just(tStatistics));
            //Act
            Mono<Void> result = service.generateStatisticFromUnconfirmedTransaction(uc);

            //Assert
            StepVerifier.create(result)
                .verifyComplete();

            verify(transactionStatisticsRepository, times(1)).save(tStatistics);
            verify(customMetrics, times(1)).incrementStatisticsPersisted();
        }
    }

    @Test
    void shouldSendAnalyticsToAdvancedProcessByStreamBridgeSuccessfully() {
         //Arrange
            when(unconfirmedTransactionRepository.save(any(UnconfirmedTransaction.class)))
                    .thenReturn(Mono.just(uc));
            when(transactionStatisticsRepository.save(any(TransactionStatistics.class)))
                    .thenReturn(Mono.just(tStatistics));
            //Act
            Mono<Void> result = service.generateStatisticFromUnconfirmedTransaction(uc);

            //Assert
            StepVerifier.create(result)
                .verifyComplete();

            verify(streamBridge, times(1)).send("sendAnalyticsToAdvancedProcess-out-0", tStatistics.getId());
    }
}
