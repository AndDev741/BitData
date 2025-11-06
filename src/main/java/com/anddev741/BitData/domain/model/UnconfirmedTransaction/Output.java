package com.anddev741.BitData.domain.model.UnconfirmedTransaction;

import lombok.Data;

@Data
public class Output {

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
