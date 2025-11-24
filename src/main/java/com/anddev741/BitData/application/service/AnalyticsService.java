package com.anddev741.BitData.application.service;

import com.anddev741.BitData.domain.model.TransactionStatistics.Receiver;
import com.anddev741.BitData.domain.model.TransactionStatistics.Sender;
import com.anddev741.BitData.domain.model.TransactionStatistics.TransactionStatistics;
import com.anddev741.BitData.domain.model.UnconfirmedTransaction.Transaction;
import com.anddev741.BitData.domain.model.UnconfirmedTransaction.UnconfirmedTransaction;
import com.anddev741.BitData.infrastructure.config.CustomMetrics;
import com.anddev741.BitData.infrastructure.persistence.TransactionStatisticsRepository;
import com.anddev741.BitData.infrastructure.persistence.UnconfirmedTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final UnconfirmedTransactionRepository unconfirmedTransactionRepository;
    private final TransactionStatisticsRepository transactionStatisticsRepository;
    private final CustomMetrics customMetrics;
    private final StreamBridge streamBridge;

    public void generateStatisticFromUnconfirmedTransaction(UnconfirmedTransaction transaction){
        //1 Step - Save the raw transaction in the database
        persistUnconfirmedTransaction(transaction);

        //2 Step - Create the metadata and statistics of the transaction
        TransactionStatistics statistics = createMetadataAndStatistics(transaction);

        //3 Step - Persist the statistics and add the ID in a queue to further advanced process
        Mono<TransactionStatistics> persistedStatistics = transactionStatisticsRepository.save(statistics);
        customMetrics.incrementStatisticsPersisted();

        persistedStatistics.subscribe(statistic -> {
            log.info("[ANALYTICS] PERSISTED STATISTICS => {}", statistic.getId());
            sendAnalyticsToAdvancedProcess(statistic.getId());
        });
    }

    private void persistUnconfirmedTransaction (UnconfirmedTransaction transaction){
        try{
            unconfirmedTransactionRepository.save(transaction).subscribe();
            customMetrics.incrementPersistedUnconfirmedTransactions();
        }catch (Exception e) {
            log.error("[SERVICE] ERROR WHEN PERSISTING RAW TRANSACTION", e);
            throw  e;
        }
    }

    private TransactionStatistics createMetadataAndStatistics (UnconfirmedTransaction transaction) {
        TransactionStatistics statistics = new TransactionStatistics();
        Transaction t = transaction.getX();

        statistics.setHash(t.getHash());

        List<Sender> senderList = new ArrayList<>();
        AtomicLong totalInput = new AtomicLong();
        t.getInputs().forEach(sender -> {
            Sender newSender = new Sender();

            newSender.setSenderAddress(sender.getPrev_out().getAddr());
            newSender.setValueSent(sender.getPrev_out().getValue());
            senderList.add(newSender);
            totalInput.addAndGet(sender.getPrev_out().getValue());
        });

        statistics.setSenderList(senderList);
        statistics.setTotalInputValue(totalInput.get());

        List<Receiver> receiverList = new ArrayList<>();
        AtomicLong totalOutput = new AtomicLong();
        t.getOut().forEach(receiver -> {
            Receiver newReceiver = new Receiver();

            newReceiver.setReceiverAddress(receiver.getAddr());
            newReceiver.setValueReceived(receiver.getValue());
            receiverList.add(newReceiver);
            totalOutput.addAndGet(receiver.getValue());
        });

        statistics.setReceiverList(receiverList);
        statistics.setTotalOutputValue(totalOutput.get());
        statistics.setFee(totalInput.get() - totalOutput.get());
        statistics.setFeeTax((double) statistics.getFee() / t.getSize());

        statistics.setInputCount(t.getInputs().size());
        statistics.setOutputCount(t.getOut().size());
        statistics.setSize(t.getSize());
        statistics.setTimestamp(t.getTime());

        return statistics;
    }

    private void sendAnalyticsToAdvancedProcess(String id){
        boolean sent = streamBridge.send("sendAnalyticsToAdvancedProcess-out-0", id);

        if(!sent){
            log.error("[ANALYTICS] ERROR SENDING ANALYTICS ID {} TO ADVANCED PROCESS TOPIC", id);
        }
    }
}
