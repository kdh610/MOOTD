package com.bwd4.mootd.repository;

import com.bwd4.mootd.domain.Photo;
import com.bwd4.mootd.dto.response.MapResponseDTO;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

import java.util.List;

public interface PhotoRepository extends ReactiveMongoRepository<Photo, String> {
    Flux<Photo> findByCoordinatesNear(Point location, Distance radius);
}
