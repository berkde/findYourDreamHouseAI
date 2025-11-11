package com.dreamhouse.ai.house.controller;

import com.dreamhouse.ai.authentication.model.security.User;
import com.dreamhouse.ai.house.dto.HouseAdDTO;
import com.dreamhouse.ai.house.dto.HouseAdImageDTO;
import com.dreamhouse.ai.house.dto.HouseAdMessageDTO;
import com.dreamhouse.ai.house.model.request.CreateHouseAdRequestModel;
import com.dreamhouse.ai.house.model.request.HouseAdMessageSendRequestModel;
import com.dreamhouse.ai.house.model.request.UpdateHouseAdTitleAndDescriptionRequestModel;
import com.dreamhouse.ai.house.model.response.LikeHouseAdResponse;
import com.dreamhouse.ai.house.service.impl.HouseAdsServiceImpl;
import com.dreamhouse.ai.cloud.service.impl.StorageServiceImpl;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.DeleteOperation;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("/api/v1/houseAds")
@Validated
public class HouseAdController {
    private static final Logger log = LoggerFactory.getLogger(HouseAdController.class);
    private final HouseAdsServiceImpl houseAdsService;
    private final StorageServiceImpl storageService;
    private final ModelMapper modelMapper;

    @Autowired
    public HouseAdController(HouseAdsServiceImpl houseAdsService,
                             StorageServiceImpl storageService,
                             ModelMapper modelMapper) {
        this.houseAdsService = houseAdsService;
        this.storageService = storageService;
        this.modelMapper = modelMapper;
    }

    @WriteOperation
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<HouseAdDTO> createHouseAd(@RequestBody CreateHouseAdRequestModel requestModel) {
        log.info("createHouseAd - Creating house ad: {}", requestModel.title());
        var houseAd = houseAdsService.createHouseAd(requestModel);
        return ResponseEntity.ok().body(houseAd);
    }

    @WriteOperation
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_ADMIN')")
    @PutMapping("/updateTitleAndDescription")
    public ResponseEntity<HouseAdDTO> updateHouseAdTitleAndDescriptionRequestModel(@RequestBody UpdateHouseAdTitleAndDescriptionRequestModel requestModel) {
        log.info("updateHouseAd - Updating house ad: {}", requestModel.title());
        var houseAd = houseAdsService.updateHouseAdTitleAndDescription(requestModel);
        return ResponseEntity.ok().body(houseAd);
    }


    @WriteOperation
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_ADMIN')")
    @PostMapping(
            value = "/{houseAdId}/images",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<List<HouseAdImageDTO>> uploadImages(
            @PathVariable String houseAdId,
            @RequestParam("files") @Valid List<MultipartFile> files,
            @RequestParam(value = "captions", required = false) List<String> captions) throws IOException {
        log.info("uploadImages - houseAdId={}, files={}", houseAdId, files.size());
        var dtos = houseAdsService.addHouseAdImages(houseAdId, files, captions);
        return ResponseEntity.status(HttpStatus.CREATED).body(dtos);
    }


    @ReadOperation
    @GetMapping("/{houseAdId}/images")
    public ResponseEntity<List<HouseAdImageDTO>> listImages(@PathVariable String houseAdId) {
        var ad = houseAdsService.getHouseAdByHouseId(houseAdId);

        var dtos = ad.getImages().stream()
                .map(img -> {
                    var dto = modelMapper.map(img, HouseAdImageDTO.class);
                    if (img.getStorageKey() != null && !img.getStorageKey().isBlank()) {
                        var url = storageService.presignedGetUrl(img.getStorageKey(), Duration.ofDays(7 )).orElse("undefined");
                        dto.setViewUrl(url);
                    }
                    return dto;
                })
                .toList();

        return ResponseEntity.ok(dtos);
    }


    @DeleteOperation
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_ADMIN')")
    @DeleteMapping("/{houseAdUid}")
    public ResponseEntity<Boolean> deleteHouseAd(@PathVariable("houseAdUid") String houseAdUid) {
        log.info("deleteHouseAd - Deleting house ad: {}", houseAdUid);
        var success = houseAdsService.deleteHouseAd(houseAdUid);
        return ResponseEntity.ok().body(success);
    }


    @DeleteOperation
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_ADMIN')")
    @DeleteMapping("/{houseAdId}/images/{imageUid}")
    public ResponseEntity<Void> deleteImage(@PathVariable String houseAdId,
                                            @PathVariable String imageUid) {
        log.info("deleteImage - houseAdId={}, imageUid={}", houseAdId, imageUid);
        boolean removed = houseAdsService.removeHouseAdImageAndObjectStore(houseAdId, imageUid);
        return removed ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }


    @ReadOperation
    @GetMapping("/id/{houseAdId}")
    public ResponseEntity<HouseAdDTO> getHouseAdByHouseId(@PathVariable String houseAdId) {
        log.info("getHouseAdByHouseId - Getting house ad by id: {}", houseAdId);
        var houseAd = houseAdsService.getHouseAdByHouseId(houseAdId);
        return ResponseEntity.ok().body(houseAd);
    }


    @ReadOperation
    @GetMapping
    public ResponseEntity<List<HouseAdDTO>> getAllHouseAds() {
        log.info("getAllHouseAds - Getting all house ads");
        var houseAds = houseAdsService.getAllHouseAds();
        return ResponseEntity.ok().body(houseAds);
    }

    @WriteOperation
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_ADMIN')")
    @PostMapping("/message")
    public ResponseEntity<HouseAdMessageDTO> sendHouseAdMessage(@RequestBody HouseAdMessageSendRequestModel requestModel) {
        log.info("sendHouseAdMessage - Sending house ad message: {}", requestModel);
        var sentMessage = houseAdsService.sendHouseAdMessage(requestModel);
        return ResponseEntity.ok().body(sentMessage);
    }


    @ReadOperation
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_ADMIN')")
    @GetMapping("/message/{messageUid}")
    public ResponseEntity<HouseAdMessageDTO> findByMessageUid(@PathVariable("messageUid") String messageUid) {
        log.info("findByMessageUid - Getting house ad message by uid: {}", messageUid);
        var houseAdMessage = houseAdsService.findByMessageUid(messageUid);
        return ResponseEntity.ok().body(houseAdMessage.orElse(null));
    }

    @ReadOperation
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_ADMIN')")
    @GetMapping("/messages/{houseUid}")
    public ResponseEntity<List<HouseAdMessageDTO>> findAllByHouseAdUid(@PathVariable("houseUid") String houseAdUid) {
        log.info("findAllByHouseAdUid - Getting house ad messages by house ad uid: {}", houseAdUid);
        var houseAdMessages = houseAdsService.findAllMessagesByHouseAdUid(houseAdUid);
        return ResponseEntity.ok().body(houseAdMessages);
    }

    @ReadOperation
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_ADMIN')")
    @GetMapping("/houseAd/{houseAdId}/like")
    public ResponseEntity<LikeHouseAdResponse> getHouseLikes(@PathVariable("houseAdId") String houseAdId) {
        log.info("Getting house ad likes for houseAd: {}", houseAdId);
        var houseAdLikes = houseAdsService.getPostLikers(houseAdId);
        return ResponseEntity.ok().body(houseAdLikes);
    }

    @WriteOperation
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_ADMIN')")
    @PostMapping("/houseAd/{houseAdId}/like")
    public ResponseEntity<?> postHouseLike(
            @PathVariable("houseAdId") String houseAdId,
            @AuthenticationPrincipal Authentication authentication) {
        log.info("Liking house ad: {}", houseAdId);
        var username = ((User)authentication.getPrincipal()).getUsername();
        var isLikeSuccessful = houseAdsService.likePost(houseAdId, username);
        return (isLikeSuccessful == Boolean.TRUE ? ResponseEntity.ok():
                        ResponseEntity.internalServerError()).build();
    }

    @WriteOperation
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_ADMIN')")
    @DeleteMapping("/houseAd/{houseAdId}/like")
    public ResponseEntity<?> removeHouseLike(
            @PathVariable("houseAdId") String houseAdId,
            @AuthenticationPrincipal Authentication authentication) {
        log.info("Liking house ad: {}", houseAdId);
        var username = ((User)authentication.getPrincipal()).getUsername();
        var isLikeSuccessful = houseAdsService.removePostLike(houseAdId, username);
        return (isLikeSuccessful == Boolean.TRUE ? ResponseEntity.ok():
                ResponseEntity.internalServerError()).build();
    }


}
