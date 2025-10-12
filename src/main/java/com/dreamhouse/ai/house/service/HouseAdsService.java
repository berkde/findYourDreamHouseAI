package com.dreamhouse.ai.house.service;

import com.dreamhouse.ai.house.dto.HouseAdDTO;
import com.dreamhouse.ai.house.dto.HouseAdImageDTO;
import com.dreamhouse.ai.house.dto.HouseAdMessageDTO;
import com.dreamhouse.ai.house.model.request.CreateHouseAdRequestModel;
import com.dreamhouse.ai.house.model.request.HouseAdMessageSendRequestModel;
import com.dreamhouse.ai.house.model.request.UpdateHouseAdTitleAndDescriptionRequestModel;
import io.micrometer.common.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface HouseAdsService {
    HouseAdDTO createHouseAd(CreateHouseAdRequestModel createHouseAdRequestModel);
    HouseAdDTO getHouseAdByHouseId(String houseAdId);
    HouseAdDTO updateHouseAdTitleAndDescription(UpdateHouseAdTitleAndDescriptionRequestModel updateHouseAdRequestModel);

    List<HouseAdImageDTO> addHouseAdImages(String houseAdId,
                                           List<MultipartFile> files,
                                           @Nullable List<String> captions) throws IOException;
    boolean removeHouseAdImageAndObjectStore(String houseAdId, String imageUid);
    Boolean deleteHouseAd(String houseAdId);

    List<HouseAdDTO> getAllHouseAdsByTitle(String title);

    List<HouseAdDTO> getAllHouseAds();

    HouseAdMessageDTO sendHouseAdMessage(HouseAdMessageSendRequestModel requestModel);
    Optional<HouseAdMessageDTO> findByMessageUid(String messageUid);
    List<HouseAdMessageDTO> findAllByHouseAdUid(String houseAdUid);

    List<HouseAdDTO> getHouseAdsWithPagination(int page, int size, String sortBy);
    List<HouseAdDTO> searchHouseAdsWithPagination(String query, int page, int size);
    Optional<HouseAdDTO> getHouseAdDetails(String houseAdId);


}

