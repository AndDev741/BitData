package com.anddev741.BitData.domain.model.UnconfirmedTransaction;

import lombok.Data;

@Data
public class Input {

    /** The sequence number, used for transaction replacement. */
    private long sequence;

    /** Information about the previous output being spent. */
    private PrevOut prev_out;

    /** The input script (signature script). Often empty in SegWit. */
    private String script;
}
