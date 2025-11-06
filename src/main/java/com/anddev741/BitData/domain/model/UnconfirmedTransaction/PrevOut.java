package com.anddev741.BitData.domain.model.UnconfirmedTransaction;

import lombok.Data;

@Data
public class PrevOut {

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
