package com.anddev741.BitData.utils;

import com.anddev741.BitData.domain.model.UnconfirmedTransaction.UnconfirmedTransaction;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ValidUnconfirmedTransaction {
    private static final ObjectMapper mapper = new ObjectMapper();

    
    public static UnconfirmedTransaction getValidUnconfirmedTransaction() throws JsonMappingException, JsonProcessingException{
        return mapper.readValue(ValidJson.getValidJson(), UnconfirmedTransaction.class);
    }
}
