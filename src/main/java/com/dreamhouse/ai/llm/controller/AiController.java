package com.dreamhouse.ai.llm.controller;

import com.dreamhouse.ai.house.repository.HouseAdRepository;
import com.dreamhouse.ai.llm.agent.conversation.ConversationalistAgent;
import com.dreamhouse.ai.llm.agent.house.HouseSearchAgent;
import com.dreamhouse.ai.llm.agent.keyword.KeywordExtractorAgent;
import com.dreamhouse.ai.llm.agent.router.RouterAgent;
import com.dreamhouse.ai.llm.model.dto.HouseSearchDTO;
import com.dreamhouse.ai.llm.model.auxilary.RequestCategory;
import com.dreamhouse.ai.llm.model.reply.ChatReply;
import com.dreamhouse.ai.llm.model.reply.ListingsReply;
import com.dreamhouse.ai.llm.model.reply.SearchReply;
import com.dreamhouse.ai.llm.model.request.ChatRequest;
import com.dreamhouse.ai.llm.service.impl.AITokenServiceImpl;
import com.dreamhouse.ai.llm.service.impl.ImageSimilaritySearchServiceImpl;
import com.dreamhouse.ai.llm.tool.HouseSearchTool;
import com.dreamhouse.ai.llm.util.AIUtil;
import com.dreamhouse.ai.mapper.HouseAdMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/ai")
@Validated
public class AiController {
    private static final Logger log = LoggerFactory.getLogger(AiController.class);
    private final ImageSimilaritySearchServiceImpl imageSimilaritySearchService;
    private final AITokenServiceImpl aiTokenService;
    private final AIUtil aiUtil;
    private final HouseSearchTool houseSearchTool;
    private final HouseAdRepository houseAdRepository;
    private final HouseAdMapper houseAdMapper;
    private final RouterAgent routerAgent;
    private final ConversationalistAgent conversationalistAgent;
    private final KeywordExtractorAgent keywordExtractorAgent;
    private final HouseSearchAgent houseSearchAgent;

    @Autowired
    public AiController(ImageSimilaritySearchServiceImpl imageSimilaritySearchService,
                        AITokenServiceImpl aiTokenService,
                        AIUtil aiUtil,
                        HouseSearchTool houseSearchTool,
                        HouseAdRepository houseAdRepository,
                        HouseAdMapper houseAdMapper, RouterAgent routerAgent, ConversationalistAgent conversationalistAgent, KeywordExtractorAgent keywordExtractorAgent, HouseSearchAgent houseSearchAgent) {
        this.imageSimilaritySearchService = imageSimilaritySearchService;
        this.aiTokenService = aiTokenService;
        this.aiUtil = aiUtil;
        this.houseSearchTool = houseSearchTool;
        this.houseAdRepository = houseAdRepository;
        this.houseAdMapper = houseAdMapper;
        this.routerAgent = routerAgent;
        this.conversationalistAgent = conversationalistAgent;
        this.keywordExtractorAgent = keywordExtractorAgent;
        this.houseSearchAgent = houseSearchAgent;
    }


    @WriteOperation
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @PostMapping(value = "/search")
    public ResponseEntity<SearchReply> search(
            @RequestHeader(value = "X-Session-Id", required = false) String headerSessionId,
            @RequestHeader(value = "X-API-Token") String x_api_token,
            @RequestBody ChatRequest request,
            HttpServletRequest httpReq,
            Principal principal) {

        String username = principal.getName();
        System.out.println(x_api_token);

        if (aiTokenService.isTokenValid(x_api_token, username) == Boolean.FALSE) {
            return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new ChatReply("AI access requires a valid token"));
        }

        String userMessage = Objects.requireNonNull(request.query(), "Message is required");
        String sessionId = aiUtil.resolveSessionId(headerSessionId, principal, httpReq);
        aiTokenService.consumeQuota(x_api_token, username);

        if(routerAgent.classify(userMessage).equals(RequestCategory.CHAT)){
            var reply = conversationalistAgent.chat(sessionId, userMessage);
            return ResponseEntity.ok(new ChatReply(reply));
        } else {
            var filter = keywordExtractorAgent.getFilterSpec(userMessage);
            HouseSearchDTO houseSearchDTO;
            try {
                houseSearchDTO = houseSearchTool.searchHouses(filter);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ChatReply("Search was interrupted, please try again."));
            }
            return ResponseEntity.ok(new ListingsReply(houseSearchDTO.getHouseAdDTOs(), "Houses found matching your criteria."));
        }

    }



    @WriteOperation
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @PostMapping("/similar")
    public ResponseEntity<?> similar(
            @RequestHeader(value = "X-API-Token") String x_api_token,
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "k", required = false) Integer k,
            @RequestParam(value = "cityHint", required = false) String cityHint,
            @RequestParam(value = "typeHint", required = false) String typeHint,
            @RequestParam(value = "bedsHint", required = false) Integer bedsHint,
            @RequestParam(value = "priceHint", required = false) Double priceHint
    ) {
        var username = aiUtil.getAuthenticatedUser();
        if (!aiTokenService.isTokenValid(x_api_token, username)) {
            return ResponseEntity.status(429).body(new ChatReply("AI access requires a valid token"));
        } else {
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
}
