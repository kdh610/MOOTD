package com.bwd4.mootd.repository;

import com.bwd4.mootd.domain.Photo;
import org.springframework.data.geo.Distance;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface PhotoRepository extends ReactiveMongoRepository<Photo, String> {
}
