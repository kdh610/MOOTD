package com.bwd4.mootd.repository;

import com.bwd4.mootd.domain.Device;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface DeviceRepository extends ReactiveMongoRepository<Device, Long> {
}
