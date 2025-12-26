package com.anddev741.BitData.utils;

import com.anddev741.BitData.application.service.AnalyticsService;
import com.anddev741.BitData.domain.model.TransactionStatistics.TransactionStatistics;
import com.anddev741.BitData.domain.model.UnconfirmedTransaction.UnconfirmedTransaction;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class ValidTransactionStatistics {

    public static TransactionStatistics getValidTransactionStatistics()
            throws JsonMappingException, JsonProcessingException {
        UnconfirmedTransaction transaction = ValidUnconfirmedTransaction.getValidUnconfirmedTransaction();

        return AnalyticsService.createMetadataAndStatistics(transaction);
    }
}
