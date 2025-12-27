package com.anddev741.BitData.domain.model.TransactionStatistics;

import java.time.LocalDate;

import lombok.Data;

@Data
public class Receiver {

    private String receiverAddress;

    private long valueReceived;

    private long totalInWallet;

    private LocalDate dateOfWalletQuery;

}
