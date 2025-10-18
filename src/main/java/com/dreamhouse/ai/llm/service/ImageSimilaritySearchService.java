package com.dreamhouse.ai.llm.service;

import com.dreamhouse.ai.house.model.entity.HouseAdEntity;
import com.dreamhouse.ai.llm.model.ImageSearchResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImageSimilaritySearchService {
    ImageSearchResult searchByImage(MultipartFile file,
                                    Integer k,
                                    String cityHint,
                                    String typeHint,
                                    Integer bedsHint,
                                    Double priceHint);

    List<HouseAdEntity> similarByVector(float[] query,
                                        int k,
                                        String cityHint,
                                        String typeHint,
                                        Integer anchorBeds,
                                        Double  anchorPrice);
}
