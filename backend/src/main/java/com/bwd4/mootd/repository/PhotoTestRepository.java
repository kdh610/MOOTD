package com.bwd4.mootd.repository;

import com.bwd4.mootd.domain.Photo;
import com.bwd4.mootd.domain.PhotoTest;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface PhotoTestRepository extends ReactiveMongoRepository<PhotoTest, String> {
    Flux<Photo> findByCoordinatesNear(Point location, Distance radius);


    Flux<PhotoTest> findByTagContaining(String tag);

}
