package com.anddev741.BitData.infrastructure.persistence;

import com.anddev741.BitData.domain.model.TransactionStatistics.TransactionStatistics;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionStatisticsRepository extends ReactiveMongoRepository<TransactionStatistics, String> {
}
