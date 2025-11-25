package com.anddev741.BitData.infrastructure.web;

import java.net.URI;
import java.time.Duration;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;

import com.anddev741.BitData.domain.port.in.ReceiveTransactionsPort;
import com.anddev741.BitData.infrastructure.config.CustomMetrics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class BlockchainWebSocketConnector implements ReceiveTransactionsPort {

    private final ReactorNettyWebSocketClient client; 
    private final String BLOCKCHAIN_URI = "wss://ws.blockchain.info/inv";
    private final CustomMetrics metrics;

    private Disposable currentConnection;

    @Override
    public Flux<String> receiveUnconfirmedTransactions() {
        return Flux.defer(this::connectToBlockchain)
            .onErrorResume(error -> {
                log.warn("[WEB] Error detected, retrying in 5 seconds...", error);

                metrics.incrementWebSocketRetries();

                return Mono.delay(Duration.ofSeconds(5))
                    .thenMany(receiveUnconfirmedTransactions());
            })
            .doOnComplete(() -> {
                log.info("[WEB] Connection completed, restarting in 5 seconds...");
            });
    }

    private Flux<String> connectToBlockchain() {
        return Flux.create(sink -> {
            log.info("[WEB] Connecting to Blockchain WebSocket...");

            this.currentConnection = client.execute(URI.create(BLOCKCHAIN_URI), session -> {
                return session.send(
                    Mono.fromCallable(() -> session.textMessage("{\"op\":\"unconfirmed_sub\"}"))
                )
                .thenMany(
                    session.receive()
                        .map(WebSocketMessage::getPayloadAsText)
                        .doOnNext(sink::next)
                        .doOnError(sink::error)
                        .doOnComplete(() -> {
                            log.info("[WEB] WebSocket connection closed");
                            sink.complete();
                        })
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

    public void close() {
        if(currentConnection != null && !currentConnection.isDisposed()) {
            log.info("[WEB] Closing WebSocket connection manually...");
            currentConnection.dispose();
        }
    }
    
}
