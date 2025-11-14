package com.anddev741.BitData.infrastructure.persistence;

import com.anddev741.BitData.domain.model.UnconfirmedTransaction.UnconfirmedTransaction;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UnconfirmedTransactionRepository extends ReactiveMongoRepository<UnconfirmedTransaction, String> {
}
