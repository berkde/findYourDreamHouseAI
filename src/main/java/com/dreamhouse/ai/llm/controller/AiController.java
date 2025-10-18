package com.dreamhouse.ai.llm.controller;

import com.dreamhouse.ai.llm.service.agent.HouseSearchAgent;
import com.dreamhouse.ai.llm.service.agent.ImageSearchAgent;
import com.dreamhouse.ai.llm.service.impl.ImageSimilaritySearchServiceImpl;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
@PermitAll
public class AiController {
    private final HouseSearchAgent houseSearchAgent;
    private final ImageSearchAgent imageSearchAgent;
    private final ImageSimilaritySearchServiceImpl imageSimilaritySearchService;

    @Autowired
    public AiController(@Qualifier("houseAgent") HouseSearchAgent agent,
                        @Qualifier("imageAgent") ImageSearchAgent imageSearchAgent,
                        ImageSimilaritySearchServiceImpl imageSimilaritySearchService) {
        this.houseSearchAgent = agent;
        this.imageSearchAgent = imageSearchAgent;
        this.imageSimilaritySearchService = imageSimilaritySearchService;
    }

    @PostMapping("/search")
    public ResponseEntity<String> search(@RequestBody Map<String, String> body) {
        String query = body.getOrDefault("q", "");
        return ResponseEntity.ok(houseSearchAgent.chat(query));
    }

    @PostMapping("/similar")
    public ResponseEntity<?> similar(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "k", required = false) Integer k,
            @RequestParam(value = "cityHint", required = false) String cityHint,
            @RequestParam(value = "typeHint", required = false) String typeHint,
            @RequestParam(value = "bedsHint", required = false) Integer bedsHint,
            @RequestParam(value = "priceHint", required = false) Double priceHint
    ) {
        var r = imageSimilaritySearchService.searchByImage(file, k, cityHint, typeHint, bedsHint, priceHint);
        return ResponseEntity.ok(Map.of(
                "inferredDescription", r.inferredDescription(),
                "results", r.results(),
                "appliedHints", Map.of(
                        "city", cityHint,
                        "type", typeHint,
                        "beds",bedsHint,
                        "price",priceHint
                )
        ));
    }

}
