package com.bwd4.mootd.service.query;

import com.bwd4.mootd.domain.PhotoEs;
import com.bwd4.mootd.dto.response.TagSearchResponseDTO;
import com.bwd4.mootd.repository.PhotoElasticSearchRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class PhotoQueryService {

    private final PhotoElasticSearchRepository photoElasticSearchRepository;

    @Autowired
    public PhotoQueryService(PhotoElasticSearchRepository photoElasticSearchRepository) {
        this.photoElasticSearchRepository = photoElasticSearchRepository;
    }

    /** ElasticSearch
     * 태그검색에 따라 모든 데이터 반환
     * @param tag
     * @return
     */
    public Flux<TagSearchResponseDTO> findEsByTag(String tag) {
        return photoElasticSearchRepository.findByTag(tag)
                .map(PhotoEs::toTagSearchResponseDTO);
    }

    /**
     * Elasticsearch tag검색
     * @param tag
     * @param pageable
     * @return
     */
    public Mono<Page<TagSearchResponseDTO>> findEsByTagWithLimit(String tag, Pageable pageable) {
        Flux<PhotoEs> tagFlux = photoElasticSearchRepository.findByTag(tag, pageable);
        Mono<Long> countMono = photoElasticSearchRepository.countByTag(tag);

        return tagFlux
                .map(PhotoEs::toTagSearchResponseDTO)
                .collectList()
                .zipWith(countMono,(tags, count) ->
                        new PageImpl<>(tags, pageable, count)
                );
    }

}
