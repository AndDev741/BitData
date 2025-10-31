package com.anddev741.BitData.domain.model;

import lombok.Data;

/**
 * Represents an unconfirmed Bitcoin transaction message received via WebSocket.
 */
@Data
public class UnconfirmedTransaction {

    /** The operation type. Typically "utx" for unconfirmed transaction. */
    private String op;

    /** The transaction details. */
    private Transaction x;

}

@Data
class Input {

    /** The sequence number, used for transaction replacement. */
    private long sequence;

    /** Information about the previous output being spent. */
    private PrevOut prev_out;

    /** The input script (signature script). Often empty in SegWit. */
    private String script;
}

@Data
class PrevOut {

    /** Whether this previous output has already been spent. */
    private boolean spent;

    /** Internal transaction index reference. */
    private long tx_index;

    /** The output type (usually 0 for standard tx). */
    private int type;

    /** The sender's Bitcoin address from which funds are spent. */
    private String addr;

    /** The value in satoshis of this previous output. */
    private long value;

    /** The output index number within the previous transaction. */
    private int n;

    /** The locking script (defines spending conditions). */
    private String script;
}

@Data
class Output {

    /** Whether this output has been spent by another transaction. */
    private boolean spent;

    /** Internal transaction index reference. */
    private long tx_index;

    /** The output type (usually 0 for standard transaction). */
    private int type;

    /** The recipient's Bitcoin address. */
    private String addr;

    /** The value in satoshis being sent to this address. */
    private long value;

    /** The output index number within this transaction. */
    private int n;

    /** The locking script (defines how this output can be spent). */
    private String script;
}