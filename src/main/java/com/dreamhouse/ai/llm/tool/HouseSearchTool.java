package com.dreamhouse.ai.llm.tool;

import com.dreamhouse.ai.cache.service.impl.QueryKeyServiceImpl;

import com.dreamhouse.ai.house.dto.HouseAdDTO;
import com.dreamhouse.ai.house.dto.HouseAdImageDTO;
import com.dreamhouse.ai.house.model.entity.HouseAdEntity;
import com.dreamhouse.ai.house.repository.HouseAdRepository;
import com.dreamhouse.ai.cloud.service.impl.StorageServiceImpl;
import com.dreamhouse.ai.llm.model.auxilary.FilterSpec;
import com.dreamhouse.ai.llm.model.dto.HouseSearchDTO;
import com.dreamhouse.ai.llm.model.auxilary.HouseAdSpecs;
import com.dreamhouse.ai.mapper.HouseAdImageMapper;
import com.dreamhouse.ai.mapper.HouseAdMapper;
import org.apache.commons.compress.utils.Lists;
import org.apache.logging.log4j.util.PerformanceSensitive;
import org.hibernate.exception.LockAcquisitionException;
import org.jetbrains.annotations.NotNull;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;


@Component
public class HouseSearchTool {
    private static final Logger log = LoggerFactory.getLogger(HouseSearchTool.class);
    private static final Integer MAX_PAGE_SIZE = 50;
    private static final String SORT_PROPERTY = "price";
    private static final Integer PAGE_NUMBER = 0;
    private final StorageServiceImpl storageService;
    private final HouseAdRepository repository;
    private final HouseAdSpecs houseAdSpecs;
    private final HouseAdImageMapper houseAdImageMapper;
    private final HouseAdMapper houseAdMapper;
    private final ConcurrentHashMap<String, CompletableFuture<HouseSearchDTO>> houseSearchInflight;
    private final RedissonClient redissonClient;
    private final QueryKeyServiceImpl queryKeyService;
    private final Executor executor;

    @Autowired
    public HouseSearchTool(HouseAdRepository repository,
                           HouseAdSpecs houseAdSpecs,
                           StorageServiceImpl storageService,
                           HouseAdImageMapper houseAdImageMapper,
                           HouseAdMapper houseAdMapper,
                           RedissonClient redissonClient,
                           QueryKeyServiceImpl queryKeyService,
                           ConcurrentHashMap<String, CompletableFuture<HouseSearchDTO>> houseSearchInflight,
                           @Qualifier("houseSearchExecutor") Executor executor) {
        this.repository = repository;
        this.houseAdSpecs = houseAdSpecs;
        this.storageService = storageService;
        this.houseAdImageMapper = houseAdImageMapper;
        this.houseAdMapper = houseAdMapper;
        this.redissonClient = redissonClient;
        this.queryKeyService = queryKeyService;
        this.houseSearchInflight = houseSearchInflight;
        this.executor = executor;
    }


    @PerformanceSensitive
    public HouseSearchDTO searchHouses(@NotNull FilterSpec filterSpec) throws InterruptedException {
        String lockKey = queryKeyService.lockKey(
                "house-search", 1,
                filterSpec.getCity(),
                filterSpec.getState(),
                filterSpec.getMinBeds(),
                filterSpec.getMaxPrice(),
                filterSpec.getMinPrice(),
                filterSpec.getMinSqft(),
                filterSpec.getTypes(),
                filterSpec.getPolygon(),
                filterSpec.getHasParking()
        );

        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (!lock.tryLock(3, 10, TimeUnit.SECONDS)) {
                throw new LockAcquisitionException("Request Throttled", new SQLException("Request Throttled"));
            }

            CompletableFuture<HouseSearchDTO> houseSearchReply = houseSearchInflight.computeIfAbsent(lockKey, __ ->
                    CompletableFuture.supplyAsync(() -> {
                        log.info("Searching for houses matching the given filters");

                        FilterSpec normalized = houseAdSpecs.normalizeFilter(filterSpec);
                        PageRequest pageRequest = PageRequest.of(PAGE_NUMBER, MAX_PAGE_SIZE, Sort.by(SORT_PROPERTY).ascending());
                        var spec = houseAdSpecs.byFilter(normalized);
                        List<HouseAdEntity> houseAdEntities = repository.findAll(spec, pageRequest).getContent();


                        var houseAdDTOS = Lists.<HouseAdDTO>newArrayList();
                        for (var entity : houseAdEntities) {
                            var dto = houseAdMapper.apply(entity);

                            var houseAdImageDTOs = Lists.<HouseAdImageDTO>newArrayList();
                            for (var image : entity.getImages()) {
                                var imageDTO = houseAdImageMapper.apply(image);
                                houseAdImageDTOs.add(imageDTO);
                            }
                            dto.setImages(houseAdImageDTOs);
                            houseAdDTOS.add(dto);
                        }
                        var reply = new HouseSearchDTO();
                        reply.setHouseAdDTOs(houseAdDTOS);
                        return reply;
                    }, executor)
            );

            try {
                return houseSearchReply.join();
            } finally {
                houseSearchInflight.remove(lockKey, houseSearchReply);
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
