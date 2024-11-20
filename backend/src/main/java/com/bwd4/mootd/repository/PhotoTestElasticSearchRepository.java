package com.bwd4.mootd.repository;

import com.bwd4.mootd.domain.PhotoEs;
import com.bwd4.mootd.domain.PhotoTestEs;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface PhotoTestElasticSearchRepository extends ReactiveElasticsearchRepository<PhotoTestEs,String> {

    Flux<PhotoTestEs> findByTag(String tag);
}
