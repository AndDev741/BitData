package com.anddev741.BitData.domain.port.in;

import reactor.core.publisher.Flux;

public interface ReceiveTransactionsPort {
    public Flux<String> receiveUnconfirmedTransactions();
}
