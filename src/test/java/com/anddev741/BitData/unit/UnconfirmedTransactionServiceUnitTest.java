package com.anddev741.BitData.unit;

import java.util.function.Supplier;

import com.anddev741.BitData.application.service.UnconfirmedTransactionService;
import com.anddev741.BitData.utils.ValidJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import com.anddev741.BitData.domain.model.UnconfirmedTransaction;
import com.anddev741.BitData.infrastructure.config.WebSocketMetrics;
import com.anddev741.BitData.infrastructure.web.BlockchainWebSocketConnector;

import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UnconfirmedTransactionServiceUnitTest {

    String validJson = ValidJson.getValidJson();

    @Mock
    private BlockchainWebSocketConnector webSocket;

    @Mock
    private WebSocketMetrics metrics;

    private UnconfirmedTransactionService service;

    @BeforeEach
    void setup(){
        service = new UnconfirmedTransactionService(webSocket, metrics);
    }


    @Test
    public void shouldConvertTheWebSocketResponse() {
        // Arrange
        when(webSocket.receiveUnconfirmedTransactions())
            .thenReturn(Flux.just(validJson)); 

        // Act
        
        Supplier<Flux<UnconfirmedTransaction>> supplier = service.sendUnconfirmedTransactions();

        Flux<UnconfirmedTransaction> result = supplier.get();

        // Assert
        StepVerifier.create(result)
            .expectNextMatches(
                tx -> tx.getX().getTime() == 1761907754
                &&
                tx.getX().getHash().equals("281640e906277cff4ee91167c54d940b05d53fc851291b6e6afcaaa538eaabce")
            )
        .verifyComplete();
    }

    @Test
    public void shouldIncreaseTheSuccessMetricsIfSuccessfullyConvertedResponse() {
        // Arrange
        when(webSocket.receiveUnconfirmedTransactions())
                .thenReturn(Flux.just(validJson));

        // Act

        Supplier<Flux<UnconfirmedTransaction>> supplier = service.sendUnconfirmedTransactions();

        Flux<UnconfirmedTransaction> result = supplier.get();

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(tx -> tx.getX().getTime() == 1761907754)
                .verifyComplete();  // Important to execute the method

        verify(metrics, times(1)).incrementUnconfirmedTransaction();
        verify(metrics, never()).incrementFailed();
    }

    // exceptions

    @Test
    public void shouldIncreaseTheFailureMetricsIfErrorWhenConvertingResponse() {
        // Arrange
        when(webSocket.receiveUnconfirmedTransactions())
                .thenReturn(Flux.just("invalid json"));

        // Act
        Supplier<Flux<UnconfirmedTransaction>> supplier = service.sendUnconfirmedTransactions();

        Flux<UnconfirmedTransaction> result = supplier.get();

        // Assert
        StepVerifier.create(result)
                .verifyError();  // Important to execute the method

        verify(metrics, times(1)).incrementFailed();
        verify(metrics, never()).incrementUnconfirmedTransaction();
    }
}
