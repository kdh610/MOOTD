package com.bwd4.mootd.repository;

import com.bwd4.mootd.domain.PhotoEs;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;

import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface PhotoElasticSearchRepository extends ReactiveElasticsearchRepository<PhotoEs,String> {

    Flux<PhotoEs> findByTag(String tag);
    @Query("{\"match\": {\"tag\": {\"query\":\"?0\"}}}")
    Flux<PhotoEs> findByTag(String tag, Pageable pageable);
    Mono<Long> countByTag(String tag);
}
