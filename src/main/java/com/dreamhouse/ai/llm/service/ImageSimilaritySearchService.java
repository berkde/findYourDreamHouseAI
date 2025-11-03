package com.dreamhouse.ai.llm.service;

import com.dreamhouse.ai.house.model.entity.HouseAdEntity;
import com.dreamhouse.ai.llm.dto.ImageSearchDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImageSimilaritySearchService {
    /**
     * Searches for similar house advertisements using an uploaded image.
     * @param file the image file to analyze
     * @param k the number of similar results to return
     * @param cityHint optional city filter for results
     * @param typeHint optional property type filter for results
     * @param bedsHint optional bedroom count filter for results
     * @param priceHint optional price range filter for results
     * @return ImageSearchReply containing inferred description and similar house ads
     */
    ImageSearchDTO searchByImage(MultipartFile file,
                                 Integer k,
                                 String cityHint,
                                 String typeHint,
                                 Integer bedsHint,
                                 Double priceHint);

    /**
     * Searches for similar house advertisements using vector similarity.
     * @param query the query vector for similarity search
     * @param k the number of similar results to return
     * @param cityHint optional city filter for results
     * @param typeHint optional property type filter for results
     * @param anchorBeds optional bedroom count filter for results
     * @param anchorPrice optional price range filter for results
     * @return List of HouseAdEntity containing similar house advertisements
     */
    List<HouseAdEntity> similarByVector(float[] query,
                                        int k,
                                        String cityHint,
                                        String typeHint,
                                        Integer anchorBeds,
                                        Double  anchorPrice);
}
