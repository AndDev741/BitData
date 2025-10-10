package com.anddev741.BitData.infrastructure.web;

import java.net.URI;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;

import com.anddev741.BitData.domain.port.in.ReceiveTransactionsPort;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class BlockchainWebSocketConnector implements ReceiveTransactionsPort {

    private final ReactorNettyWebSocketClient client = new ReactorNettyWebSocketClient(); 
    private final String BLOCKCHAIN_URI = "wss://ws.blockchain.info/inv";

    @SuppressWarnings("deprecation")
    public BlockchainWebSocketConnector() {
        client.setMaxFramePayloadLength(2 * 1024 * 1024);
    }

    @Override
    public Flux<String> receiveUnconfirmedTransactions() {
        return Flux.create(sink -> {
            log.info("[WEB] Connecting to Blockchain WebSocket...");

            client.execute(URI.create(BLOCKCHAIN_URI), session -> {
                return session.send(
                    Mono.just(session.textMessage("{\"op\":\"unconfirmed_sub\"}"))
                )
                .thenMany(
                    session.receive()
                        .map(WebSocketMessage::getPayloadAsText)
                        .doOnNext(sink::next)
                        .doOnError(sink::error)
                        .doOnComplete(sink::complete)   
                )
                .then();
            })
            .doOnError(err -> {
                log.error("[WEB ERROR] WebSocket connection error", err);
                sink.error(err);
            })
            .subscribe();
        });
    }
    
}
