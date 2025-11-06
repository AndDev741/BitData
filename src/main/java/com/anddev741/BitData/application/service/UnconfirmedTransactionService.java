package com.anddev741.BitData.application.service;

import java.util.Objects;
import java.util.function.Supplier;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import com.anddev741.BitData.domain.model.UnconfirmedTransaction.UnconfirmedTransaction;
import com.anddev741.BitData.infrastructure.config.WebSocketMetrics;
import com.anddev741.BitData.infrastructure.web.BlockchainWebSocketConnector;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
@Slf4j
public class UnconfirmedTransactionService {

    private final BlockchainWebSocketConnector webSocket;
    private final WebSocketMetrics metrics;
    private final ObjectMapper mapper = new ObjectMapper();

    @Bean
    public Supplier<Flux<UnconfirmedTransaction>> sendUnconfirmedTransactions(){
        log.info("Creating Supplier for unconfirmed transactions (waiting for queue to subscribe)...");
        
        return () -> {
            log.info(">>> Binder SUBSCRIBED! Starting WebSocket connection... <<<");
            Flux<String> unconfirmedTransactionsWebSocket = webSocket.receiveUnconfirmedTransactions();

            return unconfirmedTransactionsWebSocket
            .map(transaction -> {
                try{
                    UnconfirmedTransaction ut = mapper.readValue(transaction, UnconfirmedTransaction.class);
                    metrics.incrementUnconfirmedTransaction();
                    return ut;
                }catch (JsonProcessingException e){
                    log.error("[PRODUCER] ERROR TRYING TO DESEREALIZE... ", e);
                    metrics.incrementFailed();
                    return null;
                }
            })
            .filter(Objects::nonNull);
            //Good to debug if needed
            // .doOnNext(tx -> log.info("[PRODUCER] Sending transaction: {}", tx));
        };

    }
}
