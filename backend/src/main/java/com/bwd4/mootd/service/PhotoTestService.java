package com.bwd4.mootd.service;

import com.bwd4.mootd.domain.Photo;
import com.bwd4.mootd.domain.PhotoEs;
import com.bwd4.mootd.domain.PhotoTest;
import com.bwd4.mootd.domain.PhotoTestEs;
import com.bwd4.mootd.dto.response.TagSearchResponseDTO;
import com.bwd4.mootd.repository.PhotoTestElasticSearchRepository;
import com.bwd4.mootd.repository.PhotoTestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class PhotoTestService {

    private final PhotoTestRepository photoTestRepository;
    private final PhotoTestElasticSearchRepository photoTestElasticSearchRepository;
    private final ReactiveMongoTemplate reactiveMongoTemplate;

    /** MongoDB
     * 태그를 검색하면 태그가 포함된 mongodb에서 모든 사진데이터를 응답하는 service
     *
     * @param tag
     * @return
     */
    public Flux<TagSearchResponseDTO> findMongoByTag(String tag) {

        return photoTestRepository.findByTagContaining(tag)
                .map(PhotoTest::toTagSearchResponseDTO);
    }

    /** MongoDB
     * 태그검색에 따라 최대 limit의 수만큼 데이터 반환
     * @param tag
     * @param limit
     * @return
     */
    public Flux<TagSearchResponseDTO> findMongoByTagContainingWithLimit(String tag, int limit) {
        // Create a query to find PhotoTest with the given tag
        Query query = new Query(Criteria.where("tag").regex(tag));

        // Apply limit
        query.limit(limit);

        // Execute the query with ReactiveMongoTemplate
        return reactiveMongoTemplate.find(query, PhotoTest.class)
                .map(PhotoTest::toTagSearchResponseDTO);
    }

    /** ElasticSearch
     * 태그검색에 따라 모든 데이터 반환
     * @param tag
     * @return
     */
    public Flux<TagSearchResponseDTO> findEsByTag(String tag) {
        return photoTestElasticSearchRepository.findByTag(tag)
                .map(PhotoTestEs::toTagSearchResponseDTO);
    }

    /** ElasticSearch
     * 태그검색에 따라 최대 limit의 수만큼 데이터 반환
     * @param tag
     * @param limit
     * @return
     */
    public Flux<TagSearchResponseDTO> findEsByTagWithLimit(String tag, int limit) {
        return photoTestElasticSearchRepository.findByTag(tag).take(limit)
                .map(PhotoTestEs::toTagSearchResponseDTO);
    }



}
