package com.anddev741.BitData.domain.model.TransactionStatistics;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document
public class TransactionStatistics {

    @Id
    private String id;

    private String hash;

    private List<Sender> senderList;

    private List<Receiver> receiverList;

    private long totalInputValue;

    private long totalOutputValue;

    private long fee;

    private double feeTax;

    private long inputCount;

    private int outputCount;

    private int size;

    private long timestamp;

}
