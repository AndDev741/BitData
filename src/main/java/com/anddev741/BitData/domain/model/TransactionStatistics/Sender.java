package com.anddev741.BitData.domain.model.TransactionStatistics;

import java.time.LocalDate;

import lombok.Data;

@Data
public class Sender {
    private String senderAddress;

    private long valueSent;

    private long totalInWallet;

    private LocalDate dateOfWalletQuery;
}
