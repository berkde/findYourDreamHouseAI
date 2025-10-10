package com.dreamhouse.ai.house.service.impl;

import com.dreamhouse.ai.authentication.repository.UserRepository;
import com.dreamhouse.ai.house.dto.HouseAdDTO;
import com.dreamhouse.ai.house.dto.HouseAdImageDTO;
import com.dreamhouse.ai.house.dto.HouseAdMessageDTO;
import com.dreamhouse.ai.house.exception.*;
import com.dreamhouse.ai.house.model.entity.HouseAdEntity;
import com.dreamhouse.ai.house.model.entity.HouseAdImageEntity;
import com.dreamhouse.ai.house.model.entity.HouseAdMessageEntity;
import com.dreamhouse.ai.house.model.request.CreateHouseAdRequestModel;
import com.dreamhouse.ai.house.model.request.HouseAdMessageSendRequestModel;
import com.dreamhouse.ai.house.model.request.UpdateHouseAdTitleAndDescriptionRequestModel;
import com.dreamhouse.ai.house.model.response.StoragePutResult;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
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

    @Autowired
    public HouseAdsServiceImpl(HouseAdRepository houseAdRepository,
                               UserRepository userRepository,
                               StorageService storageService,
                               ModelMapper modelMapper,
                               HouseAdMessageRepository houseAdMessageRepository) {
        this.houseAdRepository = houseAdRepository;
        this.userRepository = userRepository;
        this.storageService = storageService;
        this.modelMapper = modelMapper;
        this.houseAdMessageRepository = houseAdMessageRepository;
    }

    @Transactional
    @CacheEvict(value = {"houseAdsList", "houseAdsSearch"}, allEntries = true)
    @Override
    public HouseAdDTO createHouseAd(CreateHouseAdRequestModel createHouseAdRequestModel) {
        try {
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


            var savedHouseAd = houseAdRepository.save(houseAd);

            return modelMapper.map(savedHouseAd, HouseAdDTO.class);
        } catch (Exception e) {
            log.error("createHouseAd - Error creating house ad: {}", createHouseAdRequestModel.title(), e);
            throw new HouseAdCreationException("Error creating house ad: " + e.getMessage() + " " + createHouseAdRequestModel.title());
        }
    }

    @Override
    @Cacheable(value = "houseAds", key = "#houseAdId")
    public HouseAdDTO getHouseAdByHouseId(String houseAdId) {
        try {
            return houseAdRepository.findByHouseAdUid(houseAdId)
                    .map(houseAdEntity -> new ModelMapper().map(houseAdEntity, HouseAdDTO.class))
                    .orElseThrow(() -> new HouseAdNotFoundException("House ad not found: " + houseAdId));
        } catch (HouseAdNotFoundException e) {
            log.error("getHouseAdByHouseId - Error getting house ad by id: {}", houseAdId, e);
            throw new HouseAdNotFoundException("House ad not found: " + houseAdId);
        }
    }

    @Transactional
    @CacheEvict(value = {"houseAds", "houseAdsList", "houseAdsSearch"}, key = "#updateHouseAdRequestModel.houseAdId()", allEntries = true)
    @Override
    public HouseAdDTO updateHouseAdTitleAndDescription(UpdateHouseAdTitleAndDescriptionRequestModel updateHouseAdRequestModel) {
        try {
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

            var savedHouseAdEntity = houseAdRepository.save(house);

            return modelMapper.map(savedHouseAdEntity, HouseAdDTO.class);
        } catch (HouseAdNotFoundException e) {
            log.error("updateHouseAdTitleAndDescription - Error updating house ad title and description: {}", updateHouseAdRequestModel.houseAdId(), e);
            throw new HouseAdTitleAndDescriptionUpdateException("Error updating house ad title and description: " + e.getMessage() + " " + updateHouseAdRequestModel.houseAdId());
        }
    }

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
            StoragePutResult put = storageService
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

    @Transactional
    @Override
    public boolean removeHouseAdImageAndObjectStore(String houseAdId, String imageUid) {
        try {
            var ad = houseAdRepository.findByHouseAdUid(houseAdId)
                    .orElseThrow(() -> new HouseAdNotFoundException("House ad not found: " + houseAdId));

            var img = ad.getImages().stream()
                    .filter(i -> imageUid.equals(i.getHouseAdImageUid()))
                    .findFirst().orElse(null);
            if (img == null) return false;

            if (img.getStorageKey() != null && !img.getStorageKey().isBlank()) {
                storageService.deleteObject(img.getStorageKey());
            }

            ad.removeImage(img);
            houseAdRepository.save(ad);
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
    @Cacheable(value = "houseAdsSearch", key = "#title")
    public List<HouseAdDTO> getAllHouseAdsByTitle(String title) {
        try {
            if (title == null || title.isBlank()) return List.of();
            log.info("Searching house ads by title: {}", title);
            return houseAdRepository.findAllByTitleContainingIgnoreCase(title)
                    .stream()
                    .map(houseAdEntity -> modelMapper.map(houseAdEntity, HouseAdDTO.class))
                    .toList();
        } catch (Exception e) {
            log.error("getAllHouseAdsByTitle - Error searching house ads by title: {}", title, e);
            return List.of();
        }
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
            return List.of();
        }
    }

    @Override
    @CacheEvict(value = {"houseAds", "houseAdsList", "houseAdsSearch"}, allEntries = true)
    public Boolean deleteHouseAd(String houseAdId) {
        try {
            log.info("Deleting house ad with id: {}", houseAdId);
            var houseAdEntity = houseAdRepository.findByHouseAdUid(houseAdId).orElseThrow(() -> new HouseAdNotFoundException("House ad not found: " + houseAdId));
            houseAdRepository.delete(houseAdEntity);
            return Boolean.TRUE;
        } catch (Exception e) {
            log.error("deleteHouseAd - Error deleting house ad: {}", houseAdId, e);
            return Boolean.FALSE;
        }
    }

    @Transactional
    @Override
    public HouseAdMessageDTO sendHouseAdMessage(HouseAdMessageSendRequestModel requestModel) {
        try {
            var receiverHouseAd = houseAdRepository.findByHouseAdUid(requestModel.getReceiverHouseAdUid())
                    .orElseThrow(() -> new HouseAdNotFoundException("House ad not found: " + requestModel.getReceiverHouseAdUid()));

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
        try {
            var message = houseAdMessageRepository.findByMessageUid(messageUid)
                    .map(houseAdMessageEntity -> modelMapper.map(houseAdMessageEntity, HouseAdMessageDTO.class))
                    .orElseThrow(() -> new HouseAdMessageException("House ad message not found: " + messageUid));
            return Optional.of(message);
        } catch (Exception e) {
            log.error("findByMessageUid - Error finding house ad message by uid: {}", messageUid, e);
            return Optional.empty();
        }
    }

    @Override
    public List<HouseAdMessageDTO> findAllByHouseAdUid(String houseAdUid) {
        try {
            var houseAd = houseAdRepository.findByHouseAdUid(houseAdUid).orElseThrow(() -> new HouseAdNotFoundException("House ad not found: " + houseAdUid));
            return houseAd.getMessages()
                    .stream()
                    .map(houseAdMessageEntity -> modelMapper.map(houseAdMessageEntity, HouseAdMessageDTO.class))
                    .toList();
        } catch (Exception e) {
            log.error("findAllByHouseAd - Error finding all house ad messages by house ad uid: {}", houseAdUid, e);
            return List.of();
        }
    }

}
