package com.dreamhouse.ai.llm.controller;

import com.dreamhouse.ai.llm.model.reply.ChatReply;
import com.dreamhouse.ai.llm.dto.HouseSearchDTO;
import com.dreamhouse.ai.llm.agent.HouseSearchAgent;
import com.dreamhouse.ai.llm.agent.ImageSearchAgent;
import com.dreamhouse.ai.llm.model.reply.ListingsReply;
import com.dreamhouse.ai.llm.model.reply.SearchReply;
import com.dreamhouse.ai.llm.model.request.ChatRequest;
import com.dreamhouse.ai.llm.service.impl.ImageSimilaritySearchServiceImpl;
import com.dreamhouse.ai.llm.util.AIUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
@Validated
public class AiController {
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
    @PostMapping(value = "/search")
    public ResponseEntity<SearchReply> search(@AuthenticationPrincipal Authentication authentication,
                                              @Valid @RequestBody ChatRequest request) {
        String query = request.query();
        String sessionId = String.valueOf((authentication.getPrincipal())).toLowerCase();
        HouseSearchDTO reply = houseSearchAgent.chat(sessionId, query);
        boolean chatOnly = reply.getHouseAdDTOS().isEmpty();

        return chatOnly ? ResponseEntity.ok(new ChatReply(reply.getAgentReply())) :
                          ResponseEntity.ok(new ListingsReply(reply.getHouseAdDTOS(), reply.getSummary()));
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
