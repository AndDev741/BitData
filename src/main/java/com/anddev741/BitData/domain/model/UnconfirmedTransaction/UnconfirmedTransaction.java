package com.anddev741.BitData.domain.model.UnconfirmedTransaction;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


/**
 * Represents an unconfirmed Bitcoin transaction message received via WebSocket.
 */
@Data
@Document(collection = "unconfirmedTransaction")
public class UnconfirmedTransaction {

    @Id
    private String id;

    /** The operation type. Typically, "utx" for unconfirmed transaction. */
    private String op;

    /** The transaction details. */
    private Transaction x;

}