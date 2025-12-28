package com.anddev741.BitData.unit;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;

import com.anddev741.BitData.application.service.AdvancedAnalyticsService;
import com.anddev741.BitData.domain.model.TransactionStatistics.TransactionStatistics;
import com.anddev741.BitData.domain.model.Wallet.Wallet;
import com.anddev741.BitData.infrastructure.config.CustomMetrics;
import com.anddev741.BitData.infrastructure.persistence.TransactionStatisticsRepository;
import com.anddev741.BitData.utils.ValidTransactionStatistics;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class AdvancedAnalyticsServiceUnitTest {
    
    @Mock
    private TransactionStatisticsRepository transactionStatisticsRepository;

    @Mock
    private CustomMetrics customMetrics;

    private AdvancedAnalyticsService service;

    TransactionStatistics tStatistics = new TransactionStatistics();
    Wallet wallet = new Wallet();
    String adress;

    @BeforeEach
    void setup() throws JsonMappingException, JsonProcessingException {

        tStatistics = ValidTransactionStatistics.getValidTransactionStatistics();
        wallet.setFinalBalance(100);
        wallet.setNTx(111);
        wallet.setTotalReceived(222);
        adress = tStatistics.getSenderList().getFirst().getSenderAddress();
    }

    @Test
    public void shouldCreateAdvancedAnalyticsAndPersist() {
        //Arrange
        Map<String, Wallet> responseBody = Map.of(adress, wallet);
        ExchangeFunction exchangeFunction = request -> {
            ClientResponse response = ClientResponse
                .create(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(responseBody.toString())
                .build();
            
                return Mono.just(response);
        };

        WebClient webClient = WebClient.builder()
            .exchangeFunction(exchangeFunction)
            .build();

        when(transactionStatisticsRepository.findById(tStatistics.getId()))
            .thenReturn(Mono.just(tStatistics));

        service = new AdvancedAnalyticsService(transactionStatisticsRepository, webClient, customMetrics);

        //Act
        Mono<Void> result = service.aggregateAdvancedAnalytics(tStatistics.getId());

        //Assert
        StepVerifier.create(result)
            .verifyComplete();

        verify(transactionStatisticsRepository, times(1)).findById(tStatistics.getId());
        verify(customMetrics, times(1)).incrementAdvancedStatisticsPersisted();

    }
}
