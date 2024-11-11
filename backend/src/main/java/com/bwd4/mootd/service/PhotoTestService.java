package com.bwd4.mootd.service;

import com.bwd4.mootd.domain.Photo;
import com.bwd4.mootd.domain.PhotoTest;
import com.bwd4.mootd.domain.PhotoUsage;
import com.bwd4.mootd.domain.PhotoUsageHistory;
import com.bwd4.mootd.dto.internal.UploadResult;
import com.bwd4.mootd.dto.request.PhotoUploadRequestDTO;
import com.bwd4.mootd.dto.request.PhotoUsageRequestDTO;
import com.bwd4.mootd.dto.response.MapResponseDTO;
import com.bwd4.mootd.dto.response.PhotoDetailDTO;
import com.bwd4.mootd.dto.response.TagSearchResponseDTO;
import com.bwd4.mootd.dto.response.TagSearchTestDTO;
import com.bwd4.mootd.enums.ImageType;
import com.bwd4.mootd.repository.PhotoRepository;
import com.bwd4.mootd.repository.PhotoTestRepository;
import com.bwd4.mootd.repository.PhotoUsageHistoryRepository;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PhotoTestService {

    private final PhotoTestRepository photoTestRepository;
    private final ReactiveMongoTemplate reactiveMongoTemplate;

    /**
     * 태그를 검색하면 태그가 포함된 mongodb에서 사진데이터를 응답하는 service
     *
     * @param tag
     * @return
     */
    public Flux<TagSearchTestDTO> searchTag(String tag) {

        return photoTestRepository.findByTagContaining(tag)
                .map(PhotoTest::toTagSearchTestDTO);
    }

    public Flux<TagSearchTestDTO> findByTagContainingWithLimit(String tag, int limit) {
        // Create a query to find PhotoTest with the given tag
        Query query = new Query(Criteria.where("tag").regex(tag));

        // Apply limit
        query.limit(limit);

        // Execute the query with ReactiveMongoTemplate
        return reactiveMongoTemplate.find(query, PhotoTest.class)
                .map(PhotoTest::toTagSearchTestDTO);
    }



}
