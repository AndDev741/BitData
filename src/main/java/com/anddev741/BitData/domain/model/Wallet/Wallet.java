package com.anddev741.BitData.domain.model.Wallet;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class Wallet {

    @JsonProperty("final_balance")
    private long finalBalance;

    @JsonProperty("n_tx")
    private long nTx;

    @JsonProperty("total_received")
    private long totalReceived;

}
