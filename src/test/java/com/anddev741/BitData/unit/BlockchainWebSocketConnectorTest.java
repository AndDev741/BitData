package com.anddev741.BitData.unit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;

import org.junit.Test;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;

import com.anddev741.BitData.infrastructure.web.BlockchainWebSocketConnector;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class BlockchainWebSocketConnectorTest {
    
    @Test
    public void shouldReceiveFluxMessages(){
        //Arrange
        ReactorNettyWebSocketClient mockClient = mock(ReactorNettyWebSocketClient.class);
        WebSocketSession mockSession = mock(WebSocketSession.class);

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

        BlockchainWebSocketConnector connector = new BlockchainWebSocketConnector(mockClient);

        //Act

        Flux<String> flux = connector.receiveUnconfirmedTransactions();

        //Assert
        StepVerifier.create(flux.take(2))
            .expectNext("tx1", "tx2")
            .verifyComplete();
    }
}
