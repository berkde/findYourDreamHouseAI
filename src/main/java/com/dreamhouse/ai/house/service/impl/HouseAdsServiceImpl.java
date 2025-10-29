package com.dreamhouse.ai.house.service.impl;

import com.dreamhouse.ai.authentication.repository.UserRepository;
import com.dreamhouse.ai.cloud.exception.EmptyFileException;
import com.dreamhouse.ai.cloud.exception.NoFilesException;
import com.dreamhouse.ai.cloud.exception.UnsupportedContentException;
import com.dreamhouse.ai.house.dto.HouseAdDTO;
import com.dreamhouse.ai.house.dto.HouseAdImageDTO;
import com.dreamhouse.ai.house.dto.HouseAdMessageDTO;
import com.dreamhouse.ai.house.exception.*;
import com.dreamhouse.ai.house.model.entity.HouseAdEntity;
import com.dreamhouse.ai.house.HouseAdImageEntity;
import com.dreamhouse.ai.house.model.entity.HouseAdMessageEntity;
import com.dreamhouse.ai.listener.ImageDeleteBatchEvent;
import com.dreamhouse.ai.listener.event.ImageDeleteEvent;
import com.dreamhouse.ai.house.model.request.CreateHouseAdRequestModel;
import com.dreamhouse.ai.house.model.request.HouseAdMessageSendRequestModel;
import com.dreamhouse.ai.house.model.request.UpdateHouseAdTitleAndDescriptionRequestModel;
import com.dreamhouse.ai.cloud.model.StoragePutResponse;
import com.dreamhouse.ai.house.repository.HouseAdMessageRepository;
import com.dreamhouse.ai.house.repository.HouseAdRepository;
import com.dreamhouse.ai.house.service.HouseAdsService;
import com.dreamhouse.ai.cloud.service.StorageService;
import com.dreamhouse.ai.mapper.HouseAdImageMapper;
import com.dreamhouse.ai.mapper.HouseAdMapper;
import com.dreamhouse.ai.mapper.HouseAdMessageMapper;
import io.micrometer.common.lang.Nullable;
import org.apache.commons.compress.utils.Sets;
import org.jetbrains.annotations.NotNull;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.util.*;

@Service
public class HouseAdsServiceImpl implements HouseAdsService {
    private final static Logger log = LoggerFactory.getLogger(HouseAdsServiceImpl.class);
    private final static String SORT_PROPERTY_PARAMETER = "createdAt";
    private final static Set<String> ALLOWED_SORT_ATTRIBUTES = Sets.newHashSet("price", "beds", "baths", "sqft", "yearBuilt", "title", "createdAt", "updatedAt");
    private final static String OBJECT_KEY_PREFIX = "house-ads/%s/%s%s";
    private final static String ALTERNATIVE_FILE_NAME = "image";
    private final HouseAdRepository houseAdRepository;
    private final UserRepository userRepository;
    private final HouseAdMessageRepository houseAdMessageRepository;
    private final StorageService storageService;
    private final HouseAdMapper houseMapper;
    private final HouseAdImageMapper houseImageMapper;
    private final HouseAdMessageMapper houseAdMessageMapper;
    private final ApplicationEventPublisher publisher;

    @Autowired
    public HouseAdsServiceImpl(HouseAdRepository houseAdRepository,
                               UserRepository userRepository,
                               HouseAdMessageRepository houseAdMessageRepository,
                               StorageService storageService,
                               HouseAdMapper houseMapper,
                               HouseAdImageMapper houseImageMapper,
                               HouseAdMessageMapper houseAdMessageMapper,
                               ApplicationEventPublisher publisher) {
        this.houseAdRepository = houseAdRepository;
        this.userRepository = userRepository;
        this.houseAdMessageRepository = houseAdMessageRepository;
        this.storageService = storageService;
        this.houseMapper = houseMapper;
        this.houseImageMapper = houseImageMapper;
        this.houseAdMessageMapper = houseAdMessageMapper;
        this.publisher = publisher;
    }

    /**
     * Creates a new house advertisement.
     * @param createHouseAdRequestModel the request model containing house ad details
     * @return HouseAdDTO containing the created house ad information
     */
    @Transactional
    @CacheEvict(value = {"houseAdsList", "houseAdsSearch"}, allEntries = true)
    @Override
    public HouseAdDTO createHouseAd(CreateHouseAdRequestModel createHouseAdRequestModel) {
            log.info("Creating house ad with title: {}", createHouseAdRequestModel.title());

            var auth = SecurityContextHolder.getContext().getAuthentication();
            String username = (auth != null ? auth.getName() : null);

            var user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Authenticated user not found"));

            HouseAdEntity houseAd = new HouseAdEntity();
            houseAd.setHouseAdUid(UUID.randomUUID().toString());
            houseAd.setTitle(createHouseAdRequestModel.title());
            houseAd.setDescription(createHouseAdRequestModel.description());
            houseAd.setCity(createHouseAdRequestModel.city());
            houseAd.setUser(user);

            if (createHouseAdRequestModel.images() != null){
                createHouseAdRequestModel.images()
                        .forEach(image -> {
                            var key = image.getStorageKey();
                            if (key != null && !key.isBlank()) {
                                String viewUrl = storageService.presignedGetUrl(key, Duration.ofMinutes(30)).orElse("undefined");
                                image.setViewUrl(viewUrl);
                            }
                            HouseAdImageEntity houseAdImageEntity = new HouseAdImageEntity();
                            houseAdImageEntity.setHouseAdImageUid(UUID.randomUUID().toString());
                            houseAdImageEntity.setImageName(image.getImageName());
                            houseAdImageEntity.setImageURL(image.getImageURL());
                            houseAdImageEntity.setImageDescription(image.getImageDescription());
                            houseAdImageEntity.setImageType(image.getImageType());
                            houseAdImageEntity.setImageThumbnail(image.getImageThumbnail());
                            houseAd.addImage(houseAdImageEntity);
                        });
            }

            var savedHouseAd = houseAdRepository.saveAndFlush(houseAd);
            return houseMapper.apply(savedHouseAd);
    }

    /**
     * Retrieves a house advertisement by its unique identifier.
     * @param houseAdId the unique identifier of the house advertisement
     * @return HouseAdDTO containing the house ad information
     */
    @Override
    @Cacheable(value = "houseAds", key = "#houseAdId")
    public HouseAdDTO getHouseAdByHouseId(String houseAdId) {
        return houseAdRepository
                .findByHouseAdUid(houseAdId)
                .map(houseMapper)
                .orElseThrow(() -> {
                    log.error("getHouseAdByHouseId - Error getting house ad by id");
                    return new HouseAdNotFoundException("House ad not found");
                });
    }

    /**
     * Updates the title and description of a house advertisement.
     * @param updateHouseAdRequestModel the request model containing updated title and description
     * @return HouseAdDTO containing the updated house ad information
     */
    @Transactional
    @CacheEvict(value = {"houseAds", "houseAdsList", "houseAdsSearch"}, key = "#updateHouseAdRequestModel.houseAdId()")
    @Override
    public HouseAdDTO updateHouseAdTitleAndDescription(UpdateHouseAdTitleAndDescriptionRequestModel updateHouseAdRequestModel) {
            var house = houseAdRepository
                    .findByHouseAdUid(updateHouseAdRequestModel.houseAdId())
                    .orElseThrow(() ->
                            new HouseAdNotFoundException("House ad not found")
                    );

            if (updateHouseAdRequestModel.title() != null && !updateHouseAdRequestModel.title().isBlank()) {
                house.setTitle(updateHouseAdRequestModel.title());
            }

            if (updateHouseAdRequestModel.description() != null && !updateHouseAdRequestModel.description().isBlank()) {
                house.setDescription(updateHouseAdRequestModel.description());
            }

        try {
            var savedHouseAdEntity = houseAdRepository.save(house);
            return houseMapper.apply(savedHouseAdEntity);
        } catch (HouseAdNotFoundException e) {
            log.error("updateHouseAdTitleAndDescription - Error updating house ad title and description");
            throw new HouseAdTitleAndDescriptionUpdateException("Error updating house ad title and description");
        }
    }

    /**
     * Adds multiple images to a house advertisement.
     * @param houseAdId the unique identifier of the house advertisement
     * @param files the list of image files to upload
     * @param captions optional list of captions for the images
     * @return List of HouseAdImageDTO containing the uploaded image information
     * @throws IOException if there's an error processing the files
     */
    @CacheEvict(cacheNames = {"houseAds","houseAdsList","houseAdsSearch"}, allEntries = true)
    @Transactional
    @Override
    public List<HouseAdImageDTO> addHouseAdImages(String houseAdId,
                                                  List<MultipartFile> files,
                                                  @Nullable List<String> captions) throws IOException {
        if (files == null || files.isEmpty())
            throw new NoFilesException("No files provided");

        var ad = houseAdRepository.findByHouseAdUid(houseAdId)
                    .orElseThrow(() -> new HouseAdNotFoundException("House ad not found"));

        log.info("addHouseAdImages - houseAdId");

        List<HouseAdImageEntity> entities = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);

            if (file.isEmpty()) throw new EmptyFileException("Empty file: " + file.getOriginalFilename());

            String content = Optional.ofNullable(file.getContentType()).orElse("");
            if (!content.startsWith(ALTERNATIVE_FILE_NAME + "/")) {
                throw new UnsupportedContentException("Unsupported content type");
            }

            long maxBytes = 10 * 1024L * 1024L;
            if(file.getSize() > maxBytes) throw new UnsupportedEncodingException("File size exceeds limit of 10MB");

            var objectKey = OBJECT_KEY_PREFIX.formatted(
                    houseAdId, UUID.randomUUID(), safeExtractFileName(file.getOriginalFilename()));
            StoragePutResponse put = storageService
                    .putObject(objectKey, file.getBytes(), content)
                    .orElseThrow();

            String thumbUrl = put.thumbnailUrl() != null ? put.thumbnailUrl() : put.url();


            var img = new HouseAdImageEntity();
            img.setHouseAdImageUid(UUID.randomUUID().toString());
            img.setImageName(Optional.ofNullable(file.getOriginalFilename()).orElse(ALTERNATIVE_FILE_NAME));
            img.setImageURL(put.url());
            img.setImageType(content);
            img.setImageThumbnail(thumbUrl);
            img.setImageDescription(captions != null && i < captions.size() ? captions.get(i) : null);
            img.setStorageKey(put.key());
            ad.addImage(img);
            entities.add(img);
        }

        houseAdRepository.save(ad);
        log.info("New Images added to the house Ad - houseAdId");
        return entities
                    .stream()
                    .map(houseImageMapper)
                    .toList();
    }

    @CacheEvict(cacheNames = {"houseAds","houseAdsList","houseAdsSearch"}, allEntries = true)
    @Transactional
    @Override
    public boolean removeHouseAdImageAndObjectStore(String houseAdId, String imageUid) {
        var houseAd = houseAdRepository
                .findByHouseAdUid(houseAdId)
                .orElseThrow(() -> new HouseAdNotFoundException("House ad not found"));

        var img = houseAd.getImages().stream()
                    .filter(image -> imageUid.equals(image.getHouseAdImageUid()))
                    .findFirst()
                    .orElseThrow(() -> new HouseAdNotFoundException("House ad image not found"));

        var storageKey = img.getStorageKey();
        houseAd.removeImage(img);
        houseAdRepository.save(houseAd);

        if(storageKey != null && !storageKey.isBlank()){
            publisher.publishEvent(new ImageDeleteEvent(storageKey));
        }

        return Boolean.TRUE;
    }

    @NotNull
    private static String safeExtractFileName(String name) {
        if (name == null) return "upload";
        int dot = name.lastIndexOf('.');
        return dot > -1 ? name.substring(dot) : "";
    }


    /**
     * Retrieves all house advertisements.
     * @return List of HouseAdDTO containing all house advertisements
     */
    @Override
    @Cacheable(value = "houseAdsList", key = "'all'")
    public List<HouseAdDTO> getAllHouseAds() {
        try {
            log.info("Searching all house ads");
            return houseAdRepository.findAll()
                    .stream()
                    .filter(Objects::nonNull)
                    .map(houseMapper)
                    .toList();
        } catch (Exception e) {
            log.error("getAllHouseAds - Error searching all house ads");
            throw new HouseAdNotFoundException("Error fetching house ads");
        }
    }

    /**
     * Deletes a house advertisement and all associated data.
     * @param houseAdId the unique identifier of the house advertisement to delete
     * @return Boolean indicating success of the operation
     */
    @Override
    @CacheEvict(value = {"houseAds", "houseAdsList", "houseAdsSearch"}, allEntries = true)
    @Transactional
    public Boolean deleteHouseAd(String houseAdId) {
        log.info("Deleting house ad");
        var houseAdEntity = houseAdRepository
                    .findByHouseAdUid(houseAdId)
                    .orElseThrow(() -> new HouseAdNotFoundException("House ad not found"));

        var storageKeys = houseAdEntity.getImages()
                        .stream()
                        .filter(Objects::nonNull)
                        .map(HouseAdImageEntity::getStorageKey)
                        .distinct()
                        .toList();

        houseAdRepository.delete(houseAdEntity);

        if(!storageKeys.isEmpty()) {
            publisher.publishEvent(new ImageDeleteBatchEvent(storageKeys));
            log.info("S3 House Ad images queued to be deleted");
        }

        return Boolean.TRUE;
    }

    /**
     * Sends a message regarding a house advertisement.
     * @param requestModel the request model containing message details
     * @return HouseAdMessageDTO containing the sent message information
     */
    @Transactional
    @Override
    public HouseAdMessageDTO sendHouseAdMessage(HouseAdMessageSendRequestModel requestModel) {
            var receiverHouseAd = houseAdRepository
                    .findByHouseAdUid(requestModel.getReceiverHouseAdUid())
                    .orElseThrow(() ->
                            new HouseAdNotFoundException("House ad not found"));

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
            return houseAdMessageMapper.apply(savedMessage);
        } catch (Exception e) {
            log.error("Error sending house ad message");
            throw new HouseAdMessageException("Error sending house ad message");
        }
    }

    /**
     * Finds a message by its unique identifier.
     * @param messageUid the unique identifier of the message
     * @return Optional containing the message if found
     */
    @Override
    public Optional<HouseAdMessageDTO> findByMessageUid(String messageUid) {
        var message = houseAdMessageRepository
                .findByMessageUid(messageUid)
                .map(houseAdMessageMapper)
                .orElseThrow(() -> {
                    log.error("Error finding house ad message by uid");
                    return new HouseAdMessageException("House ad message not found");
                });
        return Optional.of(message);
    }

    /**
     * Retrieves all messages for a specific house advertisement.
     * @param houseAdUid the unique identifier of the house advertisement
     * @return List of HouseAdMessageDTO containing all messages for the house ad
     */
    @Override
    public List<HouseAdMessageDTO> findAllMessagesByHouseAdUid(String houseAdUid) {
        var houseAd = houseAdRepository
                .findByHouseAdUid(houseAdUid)
                .orElseThrow(() -> new HouseAdNotFoundException("House ad not found"));

        try {
            return houseAd.getMessages()
                    .stream()
                    .filter(Objects::nonNull)
                    .map(houseAdMessageMapper)
                    .toList();
        } catch (Exception e) {
            log.error("Error finding all house ad messages by house ad uid");
            return List.of();
        }
    }


    /**
     * Retrieves all house advertisements with pagination.
     * @param page the page number (0-based)
     * @param size the number of items per page
     * @param sortBy the field to sort by
     * @param direction the sort direction (ASC or DESC)
     * @return List of HouseAdDTO containing the paginated house advertisements
     */
    @Transactional(readOnly = true)
    @Cacheable(
            value = "houseAdsList",
            key = "'houseAdsList:' + #page + ':' + #size + ':' + #sortBy + ':' + #direction.name()"
    )
    @Override
    public List<HouseAdDTO> getAllHouseAdsWithPagination(int page, int size, String sortBy, Sort.Direction direction) {
        log.info("Getting house ads with pagination");

        int p = Math.max(0, page);
        int s = Math.min(Math.max(1, size), 200);

        String sortProperty = ALLOWED_SORT_ATTRIBUTES.contains(sortBy) ? sortBy : SORT_PROPERTY_PARAMETER;

        Pageable pageable = PageRequest.of(p, s, Sort.by(direction == null ? Sort.Direction.ASC : direction, sortProperty));

        try {
            var pageResult = houseAdRepository.findAll(pageable);
            return pageResult.getContent()
                    .stream()
                    .filter(Objects::nonNull)
                    .map(houseMapper)
                    .toList();
        } catch (Exception e) {
            log.error("Error getting house ads with pagination");
            return Collections.emptyList();
        }
    }

    /**
     * Searches house advertisements with pagination.
     * @param query the search query string
     * @param page the page number (0-based)
     * @param size the number of items per page
     * @param sortBy the field to sort by
     * @param direction the sort direction (ASC or DESC)
     * @return List of HouseAdDTO containing the search results
     */
    @Transactional(readOnly = true)
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

        String sortProperty = ALLOWED_SORT_ATTRIBUTES.contains(sortBy) ? sortBy : SORT_PROPERTY_PARAMETER;
        Sort.Direction dir = (direction == null) ? Sort.Direction.DESC : direction;

        Pageable pageable = PageRequest.of(p, s, Sort.by(dir, sortProperty));

        Page<HouseAdEntity> pageResult =
                houseAdRepository.searchTitleOrDescription(q, pageable);

        return pageResult.getContent()
                .stream()
                .filter(Objects::nonNull)
                .map(houseMapper)
                .toList();
    }


}
