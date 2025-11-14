package com.anddev741.BitData.unit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.time.Duration;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;

import com.anddev741.BitData.infrastructure.config.CustomMetrics;
import com.anddev741.BitData.infrastructure.web.BlockchainWebSocketConnector;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.scheduler.VirtualTimeScheduler;

public class BlockchainWebSocketConnectorTest {
    
    @Test
    public void shouldReceiveFluxMessages(){
        Hooks.onErrorDropped(_ -> {});
        //Arrange
        ReactorNettyWebSocketClient mockClient = mock(ReactorNettyWebSocketClient.class);
        WebSocketSession mockSession = mock(WebSocketSession.class);
        CustomMetrics mockMetrics = mock(CustomMetrics.class);

        WebSocketMessage msg1 = mock(WebSocketMessage.class);
        WebSocketMessage msg2 = mock(WebSocketMessage.class);
        when(msg1.getPayloadAsText()).thenReturn("tx1");
        when(msg2.getPayloadAsText()).thenReturn("tx2");

        when(mockSession.send(any())).thenReturn(Mono.empty());
        when(mockSession.receive()).thenReturn(Flux.just(msg1, msg2));

        when(mockClient.execute(any(URI.class), any())).thenAnswer(invocation -> {
            WebSocketHandler handler = invocation.getArgument(1);
            return handler.handle(mockSession);
        });

        BlockchainWebSocketConnector connector = new BlockchainWebSocketConnector(mockClient, mockMetrics);

        //Act

        Flux<String> flux = connector.receiveUnconfirmedTransactions();

        //Assert
        StepVerifier.create(flux.take(2))
            .expectNext("tx1", "tx2")
            .expectComplete();
        
    }

    @Test
    public void shouldRetryOnError(){
        Hooks.onErrorDropped(_ -> {});
        //Arrange
        VirtualTimeScheduler.getOrSet();

        ReactorNettyWebSocketClient mockClient = mock(ReactorNettyWebSocketClient.class);
        WebSocketSession mockSession = mock(WebSocketSession.class);
        CustomMetrics mockMetrics = mock(CustomMetrics.class);
        WebSocketMessage msg = mock(WebSocketMessage.class);
        when(msg.getPayloadAsText()).thenReturn("tx_after_retry");

        when(mockSession.send(any())).thenReturn(Mono.empty());
        when(mockSession.receive()).thenReturn(Flux.just(msg));

        //first try -> error
        //second try -> success
        when(mockClient.execute(any(URI.class), any())).thenAnswer(new Answer<Mono<Void>>() {
            private boolean first = true;
            @Override
            public Mono<Void> answer(InvocationOnMock invocation){
                if(first) {
                    first = false;
                    return Mono.error(new RuntimeException("Simulated connection error"));
                } else {
                    WebSocketHandler handler = invocation.getArgument(1);
                    return handler.handle(mockSession);
                }
            }
        });

        BlockchainWebSocketConnector connector = new BlockchainWebSocketConnector(mockClient, mockMetrics);

        //Act

        Flux<String> flux = connector.receiveUnconfirmedTransactions();

        //Assert

        StepVerifier.withVirtualTime(() -> flux)
            .thenAwait(Duration.ofSeconds(5)) //retry delay
            .expectNext("tx_after_retry")
            .thenCancel()
            .verify();

        verify(mockMetrics, atLeastOnce()).incrementWebSocketRetries();
     
    }
}
