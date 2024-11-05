package com.bwd4.mootd.repository;

import com.bwd4.mootd.domain.Photo;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface PhotoRepository extends ReactiveMongoRepository<Photo, String> {

    @Query("{ 'tag': ?0 }")
    Flux<Photo> findByTagContaining(String tag);

    @Query("{'name':'?0'}")
    Mono<Photo> findByName(String name);
}
