package com.bwd4.mootd.repository;

import com.bwd4.mootd.domain.PhotoEs;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface PhotoElasticSearchRepository extends ReactiveElasticsearchRepository<PhotoEs,String> {

    Flux<PhotoEs> findByTag(String tag);
}
