package com.dreamhouse.ai.house.service;

import com.dreamhouse.ai.house.dto.HouseAdDTO;
import com.dreamhouse.ai.house.dto.HouseAdImageDTO;
import com.dreamhouse.ai.house.dto.HouseAdMessageDTO;
import com.dreamhouse.ai.house.model.request.CreateHouseAdRequestModel;
import com.dreamhouse.ai.house.model.request.HouseAdMessageSendRequestModel;
import com.dreamhouse.ai.house.model.request.UpdateHouseAdTitleAndDescriptionRequestModel;
import io.micrometer.common.lang.Nullable;
import org.springframework.data.domain.Sort;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface HouseAdsService {
    /**
     * Creates a new house advertisement.
     * @param createHouseAdRequestModel the request model containing house ad details
     * @return HouseAdDTO containing the created house ad information
     */
    HouseAdDTO createHouseAd(CreateHouseAdRequestModel createHouseAdRequestModel);
    
    /**
     * Retrieves a house advertisement by its unique identifier.
     * @param houseAdId the unique identifier of the house advertisement
     * @return HouseAdDTO containing the house ad information
     */
    HouseAdDTO getHouseAdByHouseId(String houseAdId);
    
    /**
     * Updates the title and description of a house advertisement.
     * @param updateHouseAdRequestModel the request model containing updated title and description
     * @return HouseAdDTO containing the updated house ad information
     */
    HouseAdDTO updateHouseAdTitleAndDescription(UpdateHouseAdTitleAndDescriptionRequestModel updateHouseAdRequestModel);

    /**
     * Adds multiple images to a house advertisement.
     * @param houseAdId the unique identifier of the house advertisement
     * @param files the list of image files to upload
     * @param captions optional list of captions for the images
     * @return List of HouseAdImageDTO containing the uploaded image information
     * @throws IOException if there's an error processing the files
     */
    List<HouseAdImageDTO> addHouseAdImages(String houseAdId,
                                           List<MultipartFile> files,
                                           @Nullable List<String> captions) throws IOException;
    
    /**
     * Removes an image from a house advertisement and deletes it from object storage.
     * @param houseAdId the unique identifier of the house advertisement
     * @param imageUid the unique identifier of the image to remove
     * @return boolean indicating success of the operation
     */
    boolean removeHouseAdImageAndObjectStore(String houseAdId, String imageUid);
    
    /**
     * Deletes a house advertisement and all associated data.
     * @param houseAdId the unique identifier of the house advertisement to delete
     * @return Boolean indicating success of the operation
     */
    Boolean deleteHouseAd(String houseAdId);

    /**
     * Retrieves all house advertisements.
     * @return List of HouseAdDTO containing all house advertisements
     */
    List<HouseAdDTO> getAllHouseAds();

    /**
     * Sends a message regarding a house advertisement.
     * @param requestModel the request model containing message details
     * @return HouseAdMessageDTO containing the sent message information
     */
    HouseAdMessageDTO sendHouseAdMessage(HouseAdMessageSendRequestModel requestModel);
    
    /**
     * Finds a message by its unique identifier.
     * @param messageUid the unique identifier of the message
     * @return Optional containing the message if found
     */
    Optional<HouseAdMessageDTO> findByMessageUid(String messageUid);
    
    /**
     * Retrieves all messages for a specific house advertisement.
     * @param houseAdUid the unique identifier of the house advertisement
     * @return List of HouseAdMessageDTO containing all messages for the house ad
     */
    List<HouseAdMessageDTO> findAllMessagesByHouseAdUid(String houseAdUid);

    /**
     * Retrieves all house advertisements with pagination.
     * @param page the page number (0-based)
     * @param size the number of items per page
     * @param sortBy the field to sort by
     * @param direction the sort direction (ASC or DESC)
     * @return List of HouseAdDTO containing the paginated house advertisements
     */
    List<HouseAdDTO> getAllHouseAdsWithPagination(int page, int size, String sortBy, Sort.Direction direction);
    
    /**
     * Searches house advertisements with pagination.
     * @param query the search query string
     * @param page the page number (0-based)
     * @param size the number of items per page
     * @param sortBy the field to sort by
     * @param direction the sort direction (ASC or DESC)
     * @return List of HouseAdDTO containing the search results
     */
    List<HouseAdDTO> searchAllHouseAdsWithPagination(String query, int page, int size, String sortBy, Sort.Direction direction);


}

