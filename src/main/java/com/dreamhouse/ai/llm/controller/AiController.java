package com.dreamhouse.ai.llm.controller;

import com.dreamhouse.ai.llm.model.reply.HouseSearchReply;
import com.dreamhouse.ai.llm.service.agent.HouseSearchAgent;
import com.dreamhouse.ai.llm.service.agent.ImageSearchAgent;
import com.dreamhouse.ai.llm.service.impl.ImageSimilaritySearchServiceImpl;
import com.dreamhouse.ai.llm.util.AIUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
@Validated
public class AiController {
    private static final Logger log = LoggerFactory.getLogger(AiController.class);
    private final HouseSearchAgent houseSearchAgent;
    private final ImageSearchAgent imageSearchAgent;
    private final ImageSimilaritySearchServiceImpl imageSimilaritySearchService;
    private final AIUtil aiUtil;

    @Autowired
    public AiController(@Qualifier("houseAgent") HouseSearchAgent agent,
                        @Qualifier("imageAgent") ImageSearchAgent imageSearchAgent,
                        ImageSimilaritySearchServiceImpl imageSimilaritySearchService,
                        AIUtil aiUtil) {
        this.houseSearchAgent = agent;
        this.imageSearchAgent = imageSearchAgent;
        this.imageSimilaritySearchService = imageSimilaritySearchService;
        this.aiUtil = aiUtil;
    }

    @WriteOperation
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @PostMapping("/search")
    public ResponseEntity<HouseSearchReply> search(@RequestBody Map<String, String> body) {
        String query = body.getOrDefault("q", "");
        return ResponseEntity.ok(houseSearchAgent.chat(query));
    }

    @WriteOperation
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
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
                        "beds", bedsHint,
                        "price", priceHint
                )
        ));
    }

}
