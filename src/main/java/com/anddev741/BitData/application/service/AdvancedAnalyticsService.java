package com.anddev741.BitData.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.anddev741.BitData.domain.model.Wallet.Wallet;
import com.anddev741.BitData.infrastructure.config.CustomMetrics;
import com.anddev741.BitData.infrastructure.persistence.TransactionStatisticsRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdvancedAnalyticsService {

    private final TransactionStatisticsRepository transactionStatisticsRepository;
    private final WebClient walletWebClient;
    private final CustomMetrics customMetrics;

    public Mono<Void> aggregateAdvancedAnalytics(String id) {
        // Get total in the wallet of all the receivers and senders and persist again
        // the Statistics
        log.info("STARTING ADVANCED ANALYTICS TO ID => {}", id);

        return transactionStatisticsRepository.findById(id)
                .doOnSuccess(statistic -> {
                    try{
                        statistic.getReceiverList().stream()
                            .forEach(receiver -> {
                                if(receiver == null) return;
                                fetchWallet(receiver.getReceiverAddress()).subscribe(wallet -> {
                                    if (wallet == null) {
                                        log.warn("No wallet data found for receiver {}", receiver.getReceiverAddress());
                                        return;
                                    }

                                    log.info("Wallet found for receiver {} => {}", receiver.getReceiverAddress(), wallet);

                                    receiver.setDateOfWalletQuery(LocalDate.now());
                                    receiver.setTotalInWallet(wallet.getFinalBalance());
                                    transactionStatisticsRepository.save(statistic).subscribe();
                                }, error -> log.error("Error fetching wallet for receiver {}", receiver.getReceiverAddress(), error));
                            });
                        statistic.getSenderList().stream()
                            .forEach(sender -> {
                                if(sender == null) return;
                                fetchWallet(sender.getSenderAddress()).subscribe(wallet -> {
                                    if (wallet == null) {
                                        log.warn("No wallet data found for sender {}", sender.getSenderAddress());
                                        return;
                                    }

                                    log.info("Wallet found for sender {} => {}", sender.getSenderAddress(), wallet);

                                    sender.setDateOfWalletQuery(LocalDate.now());
                                    sender.setTotalInWallet(wallet.getFinalBalance());
                                    transactionStatisticsRepository.save(statistic).subscribe();
                                }, error -> log.error("Error fetching wallet for sender {}", sender.getSenderAddress(), error));
                            });
                        customMetrics.incrementAdvancedStatisticsPersisted();
                    }catch(Exception e){
                        log.error("Error processing advanced analytics => {}", statistic.getId());
                        customMetrics.incrementAdvancedStatisticsFailed();
                    }
                }).then();
    }

    private Mono<Wallet> fetchWallet(String address) {
        return walletWebClient.get()
                .uri(uriBuilder -> uriBuilder.path("/balance").queryParam("active", address).build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Wallet>>() {
                })
                .map(map -> map.get(address));
    }
}
