package com.anddev741.BitData.domain.model;

import java.util.List;

import lombok.Data;

@Data
public class Transaction {
    /** The lock time for the transaction (usually 0 for unconfirmed txs). */
    private long lock_time;

    /** The version number of the transaction format. */
    private int ver;

    /** The total size of the transaction in bytes. */
    private int size;

    /** The list of transaction inputs (where the coins come from). */
    private List<Input> inputs;

    /** The UNIX timestamp when the transaction was received. */
    private long time;

    /** The internal transaction index in the blockchain system. */
    private long tx_index;

    /** Number of transaction inputs. */
    private int vin_sz;

    /** The unique hash identifier of the transaction. */
    private String hash;

    /** Number of transaction outputs. */
    private int vout_sz;

    /** The IP address of the node that relayed this transaction. */
    private String relayed_by;

    /** The list of outputs (where the coins are sent). */
    private List<Output> out;
}
