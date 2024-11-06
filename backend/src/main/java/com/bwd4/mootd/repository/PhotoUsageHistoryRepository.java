package com.bwd4.mootd.repository;

import com.bwd4.mootd.domain.PhotoUsageHistory;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface PhotoUsageHistoryRepository extends ReactiveMongoRepository<PhotoUsageHistory, String> {
}
