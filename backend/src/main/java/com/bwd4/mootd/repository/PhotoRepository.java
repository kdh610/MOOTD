package com.bwd4.mootd.repository;

import com.bwd4.mootd.domain.Photo;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface PhotoRepository extends ReactiveMongoRepository<Photo, String> {
}
