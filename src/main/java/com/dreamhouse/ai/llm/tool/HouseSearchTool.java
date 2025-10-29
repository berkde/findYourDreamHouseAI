package com.dreamhouse.ai.llm.tool;

import com.dreamhouse.ai.cache.service.impl.QueryKeyServiceImpl;
import com.dreamhouse.ai.house.dto.HouseAdDTO;

import com.dreamhouse.ai.house.dto.HouseAdImageDTO;
import com.dreamhouse.ai.house.repository.HouseAdRepository;
import com.dreamhouse.ai.cloud.service.impl.StorageServiceImpl;
import com.dreamhouse.ai.llm.model.request.FilterSpec;
import com.dreamhouse.ai.llm.model.request.HouseAdSpecs;
import com.dreamhouse.ai.llm.model.reply.HouseSearchReply;
import dev.langchain4j.agent.tool.Tool;
import org.apache.logging.log4j.util.PerformanceSensitive;
import org.hibernate.exception.LockAcquisitionException;
import org.modelmapper.ModelMapper;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


@Component
public class HouseSearchTool {
    private static final Logger log = LoggerFactory.getLogger(HouseSearchTool.class);
    private static final Integer MAX_PAGE_SIZE = 1_000;
    private static final Integer QUERY_LIMIT = 50;
    private static final String SORT_PROPERTY = "price";
    private static final Integer PAGE_NUMBER = 0;
    private final StorageServiceImpl storageService;
    private final HouseAdRepository repository;
    private final ModelMapper mapper;
    private final ConcurrentHashMap<String, CompletableFuture<HouseSearchReply>> inflight = new ConcurrentHashMap<>();
    private final RedissonClient redissonClient;
    private final QueryKeyServiceImpl  queryKeyService;

    @Autowired
    public HouseSearchTool(HouseAdRepository repository,
                           StorageServiceImpl storageService,
                           ModelMapper mapper,
                           RedissonClient redissonClient,
                           QueryKeyServiceImpl queryKeyService) {
        this.repository = repository;
        this.storageService = storageService;
        this.mapper = mapper;
        this.redissonClient = redissonClient;
        this.queryKeyService = queryKeyService;
    }

    @Tool("Search the database for houses matching the given filters")
    @PerformanceSensitive
    public HouseSearchReply searchHouses(FilterSpec filterSpec) throws InterruptedException {
        String lockKey = queryKeyService.lockKey(
                "house-search", 1,
                filterSpec.getCity(),
                filterSpec.getState(),
                filterSpec.getMinBeds(),
                filterSpec.getMaxPrice(),
                filterSpec.getKeywords(),
                filterSpec.getMinPrice(),
                filterSpec.getMinSqft(),
                filterSpec.getTypes(),
                filterSpec.getPolygon(),
                filterSpec.getHasParking()
        );

        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (!lock.tryLock(1, 5, TimeUnit.SECONDS)) {
                throw new LockAcquisitionException("Request Throttled", new SQLException("Request Throttled"));
            }

            CompletableFuture<HouseSearchReply> houseSearchReply = inflight.computeIfAbsent(lockKey, __ ->
                    CompletableFuture.supplyAsync(() -> {
                                log.info("Searching for houses matching the given filters");
                                var spec = HouseAdSpecs.byFilter(filterSpec);
                                PageRequest pageRequest = PageRequest.of(PAGE_NUMBER, MAX_PAGE_SIZE, Sort.by(SORT_PROPERTY).ascending());

                                List<HouseAdDTO> houseAdDTOS = repository
                                        .findAll(spec, pageRequest)
                                        .stream()
                                        .map(entity -> {
                                            var houseAdImageDTOs = entity.getImages()
                                                    .stream()
                                                    .map(image -> {
                                                        var dto = mapper.map(image, HouseAdImageDTO.class);
                                                        dto.setViewUrl(storageService
                                                                .presignedGetUrl(image.getStorageKey(), Duration.ofDays(7))
                                                                .orElseThrow());
                                                        return dto;
                                                    })
                                                    .toList();
                                            var dto = mapper.map(entity, HouseAdDTO.class);
                                            dto.setImages(houseAdImageDTOs);
                                            return dto;
                                        })
                                        .limit(QUERY_LIMIT)
                                        .toList();

                                var reply = new HouseSearchReply();
                                reply.setHouseAdDTOS(houseAdDTOS);
                                return reply;
                            })
            );

            try {
                return houseSearchReply.join();
            } finally {
                inflight.remove(lockKey, houseSearchReply);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        } finally {
            if (lock.isHeldByCurrentThread())
                lock.unlock();
        }
    }
}
