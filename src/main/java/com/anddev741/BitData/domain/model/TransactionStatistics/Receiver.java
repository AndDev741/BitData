package com.anddev741.BitData.domain.model.TransactionStatistics;

import lombok.Data;

@Data
public class Receiver {

    private String receiverAddress;

    private long valueReceived;

}
