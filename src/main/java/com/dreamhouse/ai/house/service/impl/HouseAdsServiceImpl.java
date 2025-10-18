package com.dreamhouse.ai.house.service.impl;

import com.dreamhouse.ai.authentication.repository.UserRepository;
import com.dreamhouse.ai.house.dto.HouseAdDTO;
import com.dreamhouse.ai.house.dto.HouseAdImageDTO;
import com.dreamhouse.ai.house.dto.HouseAdMessageDTO;
import com.dreamhouse.ai.house.exception.*;
import com.dreamhouse.ai.house.model.entity.HouseAdEntity;
import com.dreamhouse.ai.house.model.entity.HouseAdImageEntity;
import com.dreamhouse.ai.house.model.entity.HouseAdMessageEntity;
import com.dreamhouse.ai.house.model.event.ImageDeleteEvent;
import com.dreamhouse.ai.house.model.request.CreateHouseAdRequestModel;
import com.dreamhouse.ai.house.model.request.HouseAdMessageSendRequestModel;
import com.dreamhouse.ai.house.model.request.UpdateHouseAdTitleAndDescriptionRequestModel;
import com.dreamhouse.ai.house.model.response.StoragePutResponse;
import com.dreamhouse.ai.house.repository.HouseAdMessageRepository;
import com.dreamhouse.ai.house.repository.HouseAdRepository;
import com.dreamhouse.ai.house.service.HouseAdsService;
import com.dreamhouse.ai.house.service.StorageService;
import io.micrometer.common.lang.Nullable;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.util.*;

@Service
public class HouseAdsServiceImpl implements HouseAdsService {
    private final HouseAdRepository houseAdRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;
    private final ModelMapper modelMapper;
    private final static Logger log = LoggerFactory.getLogger(HouseAdsServiceImpl.class);
    private final HouseAdMessageRepository houseAdMessageRepository;
    private final ApplicationEventPublisher publisher;

    @Autowired
    public HouseAdsServiceImpl(HouseAdRepository houseAdRepository,
                               UserRepository userRepository,
                               StorageService storageService,
                               ModelMapper modelMapper,
                               HouseAdMessageRepository houseAdMessageRepository,
                               ApplicationEventPublisher publisher) {
        this.houseAdRepository = houseAdRepository;
        this.userRepository = userRepository;
        this.storageService = storageService;
        this.modelMapper = modelMapper;
        this.houseAdMessageRepository = houseAdMessageRepository;
        this.publisher = publisher;
    }

    @Transactional
    @CacheEvict(value = {"houseAdsList", "houseAdsSearch"}, allEntries = true)
    @Override
    public HouseAdDTO createHouseAd(CreateHouseAdRequestModel createHouseAdRequestModel) {
            log.info("Creating house ad with title: {}", createHouseAdRequestModel.title());

            var auth = SecurityContextHolder.getContext().getAuthentication();
            String username = (auth != null ? auth.getName() : null);

            var user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Authenticated user not found: " + username));

            HouseAdEntity houseAd = new HouseAdEntity();
            houseAd.setHouseAdUid(UUID.randomUUID().toString());
            houseAd.setTitle(createHouseAdRequestModel.title());
            houseAd.setDescription(createHouseAdRequestModel.description());
            houseAd.setUser(user);

            if (createHouseAdRequestModel.images() != null){
                createHouseAdRequestModel.images()
                        .forEach(image -> {
                            var key = image.getStorageKey();
                            if (key != null && !key.isBlank()) {
                                image.setViewUrl(storageService.presignedGetUrl(key, Duration.ofMinutes(30)).orElse("undefined"));
                            }
                            HouseAdImageEntity houseAdImageEntity = new HouseAdImageEntity();
                            houseAdImageEntity.setHouseAdImageUid(UUID.randomUUID().toString());
                            houseAdImageEntity.setImageName(image.getImageName());
                            houseAdImageEntity.setImageUrl(image.getImageURL());
                            houseAdImageEntity.setImageDescription(image.getImageDescription());
                            houseAdImageEntity.setImageType(image.getImageType());
                            houseAdImageEntity.setImageThumbnail(image.getImageThumbnail());
                            houseAd.addImage(houseAdImageEntity);
                        });
            }

        try {
            var savedHouseAd = houseAdRepository.save(houseAd);
            return modelMapper.map(savedHouseAd, HouseAdDTO.class);
        } catch (Exception e) {
            log.error("createHouseAd - Error saving house ad: {}", createHouseAdRequestModel.title(), e);
            throw new HouseAdCreationException("Error saving house ad: " + e.getMessage() + " " + createHouseAdRequestModel.title());
        }
    }

    @Override
    @Cacheable(value = "houseAds", key = "#houseAdId")
    public HouseAdDTO getHouseAdByHouseId(String houseAdId) {
        return houseAdRepository
                .findByHouseAdUid(houseAdId)
                .map(houseAdEntity -> modelMapper.map(houseAdEntity, HouseAdDTO.class))
                .orElseThrow(() -> {
                    log.error("getHouseAdByHouseId - Error getting house ad by id: {}", houseAdId);
                    return new HouseAdNotFoundException("House ad not found: " + houseAdId);
                });
    }

    @Transactional
    @CacheEvict(value = {"houseAds", "houseAdsList", "houseAdsSearch"}, key = "#updateHouseAdRequestModel.houseAdId()", allEntries = true)
    @Override
    public HouseAdDTO updateHouseAdTitleAndDescription(UpdateHouseAdTitleAndDescriptionRequestModel updateHouseAdRequestModel) {
            var house = houseAdRepository
                    .findByHouseAdUid(updateHouseAdRequestModel.houseAdId())
                    .orElseThrow(() ->
                            new HouseAdNotFoundException("House ad not found: " +
                                     updateHouseAdRequestModel.houseAdId())
                    );

            if (updateHouseAdRequestModel.title() != null && !updateHouseAdRequestModel.title().isBlank()) {
                house.setTitle(updateHouseAdRequestModel.title());
            }

            if (updateHouseAdRequestModel.description() != null && !updateHouseAdRequestModel.description().isBlank()) {
                house.setDescription(updateHouseAdRequestModel.description());
            }

        try {
            var savedHouseAdEntity = houseAdRepository.save(house);
            return modelMapper.map(savedHouseAdEntity, HouseAdDTO.class);
        } catch (HouseAdNotFoundException e) {
            log.error("updateHouseAdTitleAndDescription - Error updating house ad title and description: {}", updateHouseAdRequestModel.houseAdId(), e);
            throw new HouseAdTitleAndDescriptionUpdateException("Error updating house ad title and description: " + e.getMessage() + " " + updateHouseAdRequestModel.houseAdId());
        }
    }

    @CacheEvict(cacheNames = {"houseAds","houseAdsList","houseAdsSearch"}, allEntries = true)
    @Transactional
    @Override
    public List<HouseAdImageDTO> addHouseAdImages(String houseAdId,
                                                  List<MultipartFile> files,
                                                  @Nullable List<String> captions) throws IOException {
        if (files == null || files.isEmpty())
            throw new NoFilesException("No files provided");

        var ad = houseAdRepository.findByHouseAdUid(houseAdId)
                    .orElseThrow(() -> new HouseAdNotFoundException("House ad not found: " + houseAdId));

        List<HouseAdImageEntity> entities = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);

            String content = Optional.ofNullable(file.getContentType()).orElse("");
            if (!content.startsWith("image/")) {
                throw new UnsupportedContentException("Unsupported content type: " + content);
            }
            if (file.isEmpty()) throw new EmptyFileException("Empty file: " + file.getOriginalFilename());

            long maxBytes = 10 * 1024 * 1024;
            if(file.getSize() > maxBytes) throw new UnsupportedEncodingException("File size exceeds limit of 10MB: " + file.getOriginalFilename() + " " + file.getSize() + " bytes");

            var objectKey = "house-ads/%s/%s%s".formatted(
                    houseAdId, UUID.randomUUID(), safeExtractFileName(file.getOriginalFilename()));
            StoragePutResponse put = storageService
                    .putObject(objectKey, file.getBytes(), content)
                    .orElseThrow();

                String thumbUrl = put.thumbnailUrl() != null ? put.thumbnailUrl() : put.url();

                var img = new HouseAdImageEntity();
                img.setHouseAdImageUid(UUID.randomUUID().toString());
                img.setImageName(Optional.ofNullable(file.getOriginalFilename()).orElse("image"));
                img.setImageUrl(put.url());
                img.setImageType(content);
                img.setImageThumbnail(thumbUrl);
                img.setImageDescription(captions != null && i < captions.size() ? captions.get(i) : null);
                img.setStorageKey(put.key());
                ad.addImage(img);
                entities.add(img);
        }

        houseAdRepository.save(ad);
        return entities
                    .stream()
                    .map(e -> modelMapper.map(e, HouseAdImageDTO.class))
                    .toList();
    }

    @CacheEvict(cacheNames = {"houseAds","houseAdsList","houseAdsSearch"}, allEntries = true)
    @Transactional
    @Override
    public boolean removeHouseAdImageAndObjectStore(String houseAdId, String imageUid) {
        var houseAd = houseAdRepository
                .findByHouseAdUid(houseAdId)
                .orElseThrow(() -> new HouseAdNotFoundException("House ad not found: " + houseAdId));

        var img = houseAd.getImages().stream()
                    .filter(image -> imageUid.equals(image.getHouseAdImageUid()))
                    .findFirst()
                    .orElse(null);

        if (img == null) throw new HouseAdImageNotFoundException("House ad image not found: " + imageUid);

        if (img.getStorageKey() != null && !img.getStorageKey().isBlank()) {
            storageService.deleteObject(img.getStorageKey());
        }

        try {
            houseAd.removeImage(img);
            houseAdRepository.save(houseAd);
            return Boolean.TRUE;
        } catch (HouseAdImageNotFoundException e) {
            log.error("removeHouseAdImageAndObjectStore - Error removing house ad image and object store: {}", imageUid, e);
            return Boolean.FALSE;
        }
    }

    private static String safeExtractFileName(String name) {
        if (name == null) return "upload";
        int dot = name.lastIndexOf('.');
        return dot > -1 ? name.substring(dot) : "";
    }


    @Override
    @Cacheable(value = "houseAdsList", key = "'all'")
    public List<HouseAdDTO> getAllHouseAds() {
        try {
            log.info("Searching all house ads");
            return houseAdRepository.findAll()
                    .stream()
                    .map(houseAdEntity -> modelMapper.map(houseAdEntity, HouseAdDTO.class))
                    .toList();
        } catch (Exception e) {
            log.error("getAllHouseAds - Error searching all house ads: {}", e.getMessage(), e);
            throw new HouseAdNotFoundException("Error fetching house ads: " + e.getMessage());
        }
    }

    @Override
    @CacheEvict(value = {"houseAds", "houseAdsList", "houseAdsSearch"}, allEntries = true)
    @Transactional
    public Boolean deleteHouseAd(String houseAdId) {
        log.info("Deleting house ad with id: {}", houseAdId);
        var houseAdEntity = houseAdRepository
                    .findByHouseAdUid(houseAdId)
                    .orElseThrow(() -> new HouseAdNotFoundException("House ad not found: " + houseAdId));

        houseAdEntity.getImages()
                    .forEach(image ->
                            publisher.publishEvent(new ImageDeleteEvent(image.getStorageKey())));

        try {
            houseAdRepository.delete(houseAdEntity);

            return Boolean.TRUE;
        } catch (Exception e) {
            log.error("deleteHouseAd - Error deleting house ad: {}", houseAdId, e);
            throw new HouseAdDeleteException(e.getMessage());
        }
    }

    @Transactional
    @Override
    public HouseAdMessageDTO sendHouseAdMessage(HouseAdMessageSendRequestModel requestModel) {
            var receiverHouseAd = houseAdRepository
                    .findByHouseAdUid(requestModel.getReceiverHouseAdUid())
                    .orElseThrow(() ->
                            new HouseAdNotFoundException("House ad not found: " + requestModel.getReceiverHouseAdUid()));

            var message = new HouseAdMessageEntity();
            message.setMessageUid(UUID.randomUUID().toString());
            message.setMessageDate(requestModel.getMessageDate());
            message.setSenderEmail(requestModel.getSenderEmail());
            message.setSenderName(requestModel.getSenderName());
            message.setSenderPhone(requestModel.getSenderPhone());
            message.setSubject(requestModel.getSubject());
            message.setMessage(requestModel.getMessage());
            message.setHouseAd(receiverHouseAd);
            receiverHouseAd.addMessage(message);

        try {
            var savedMessage = houseAdMessageRepository.save(message);
            houseAdRepository.save(receiverHouseAd);

            return modelMapper.map(savedMessage, HouseAdMessageDTO.class);
        } catch (Exception e) {
            log.error("sendHouseAdMessage - Error sending house ad message: {}", requestModel.getReceiverHouseAdUid(), e);
            throw new HouseAdMessageException("Error sending house ad message: " + e.getMessage() + " " + requestModel.getReceiverHouseAdUid());
        }
    }

    @Override
    public Optional<HouseAdMessageDTO> findByMessageUid(String messageUid) {
        var message = houseAdMessageRepository
                .findByMessageUid(messageUid)
                .map(houseAdMessageEntity ->
                        modelMapper.map(houseAdMessageEntity, HouseAdMessageDTO.class))
                .orElseThrow(() -> {
                    log.error("findByMessageUid - Error finding house ad message by uid: {}", messageUid);
                    return new HouseAdMessageException("House ad message not found: " + messageUid);
                });
        return Optional.of(message);
    }

    @Override
    public List<HouseAdMessageDTO> findAllMessagesByHouseAdUid(String houseAdUid) {
        var houseAd = houseAdRepository
                .findByHouseAdUid(houseAdUid)
                .orElseThrow(() -> new HouseAdNotFoundException("House ad not found: " + houseAdUid));

        try {
            return houseAd.getMessages()
                    .stream()
                    .map(houseAdMessageEntity ->
                            modelMapper.map(houseAdMessageEntity, HouseAdMessageDTO.class))
                    .toList();
        } catch (Exception e) {
            log.error("findAllByHouseAd - Error finding all house ad messages by house ad uid: {}", houseAdUid, e);
            return List.of();
        }
    }


    @Transactional
    @Cacheable(
            value = "houseAdsList",
            key = "'houseAdsList:' + #page + ':' + #size + ':' + #sortBy + ':' + #direction.name()"
    )
    @Override
    public List<HouseAdDTO> getAllHouseAdsWithPagination(int page, int size, String sortBy, Sort.Direction direction) {
        log.info("Getting house ads with pagination - page: {}, size: {}, sortBy: {}, dir: {}",
                page, size, sortBy, direction);

        int p = Math.max(0, page);
        int s = Math.min(Math.max(1, size), 200);

        Set<String> allowed = Set.of("price", "beds", "baths", "sqft", "yearBuilt", "title", "createdAt", "updatedAt");
        String sortProperty = allowed.contains(sortBy) ? sortBy : "createdAt";

        Pageable pageable = PageRequest.of(p, s, Sort.by(direction == null ? Sort.Direction.ASC : direction, sortProperty));

        try {
            var pageResult = houseAdRepository.findAll(pageable);
            return pageResult.getContent()
                    .stream()
                    .map(h -> modelMapper.map(h, HouseAdDTO.class))
                    .toList();
        } catch (Exception e) {
            log.error("Error getting house ads with pagination", e);
            return Collections.emptyList();
        }
    }


    @Transactional
    @Cacheable(
            value = "houseAdsSearch",
            key = "'q:' + T(org.springframework.util.StringUtils).trimAllWhitespace(#query == null ? '' : #query.toLowerCase())"
                    + " + ':p:' + #page + ':s:' + #size + ':sb:' + #sortBy + ':dir:' + #direction.name()",
            unless = "#result == null || #result.isEmpty()"
    )
    @Override
    public List<HouseAdDTO> searchAllHouseAdsWithPagination(
            String query,
            int page,
            int size,
            String sortBy,
            Sort.Direction direction
    ) {
        String q = (query == null) ? "" : query.trim();
        if (!StringUtils.hasText(q)) {
            return List.of();
        }

        int p = Math.max(0, page);
        int s = Math.min(Math.max(1, size), 200);

        Set<String> allowed = Set.of("price", "beds", "baths", "sqft", "yearBuilt", "title", "createdAt", "updatedAt");
        String sortProperty = allowed.contains(sortBy) ? sortBy : "createdAt";
        Sort.Direction dir = (direction == null) ? Sort.Direction.DESC : direction;

        Pageable pageable = PageRequest.of(p, s, Sort.by(dir, sortProperty));

        Page<HouseAdEntity> pageResult =
                houseAdRepository.searchTitleOrDescription(q, pageable);

        return pageResult.getContent()
                .stream()
                .map(h -> modelMapper.map(h, HouseAdDTO.class))
                .toList();
    }


}
