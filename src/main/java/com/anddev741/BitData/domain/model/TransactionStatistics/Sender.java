package com.anddev741.BitData.domain.model.TransactionStatistics;

import lombok.Data;

@Data
public class Sender {
    private String senderAddress;

    private long valueSent;
}
