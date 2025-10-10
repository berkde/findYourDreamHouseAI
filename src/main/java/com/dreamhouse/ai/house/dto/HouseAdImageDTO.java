package com.dreamhouse.ai.house.dto;

import java.util.Objects;

public class HouseAdImageDTO {
    private String houseAdImageID;
    private String imageURL;
    private String storageKey;
    private String imageName;
    private String imageType;
    private String imageDescription;
    private String imageThumbnail;
    private String viewUrl;

    public HouseAdImageDTO() {
    }

    public String getHouseAdImageID() {
        return houseAdImageID;
    }

    public void setHouseAdImageID(String houseAdImageID) {
        this.houseAdImageID = houseAdImageID;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public void setStorageKey(String storageKey) {
        this.storageKey = storageKey;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getImageType() {
        return imageType;
    }

    public void setImageType(String imageType) {
        this.imageType = imageType;
    }

    public String getImageDescription() {
        return imageDescription;
    }

    public void setImageDescription(String imageDescription) {
        this.imageDescription = imageDescription;
    }

    public String getImageThumbnail() {
        return imageThumbnail;
    }

    public void setImageThumbnail(String imageThumbnail) {
        this.imageThumbnail = imageThumbnail;
    }

    public String getViewUrl() {
        return viewUrl;
    }

    public void setViewUrl(String viewUrl) {
        this.viewUrl = viewUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof HouseAdImageDTO that)) return false;
        return Objects.equals(houseAdImageID, that.houseAdImageID);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(houseAdImageID);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("HouseAdImageDTO{");
        sb.append("houseAdImageID='").append(houseAdImageID).append('\'');
        sb.append(", imageName='").append(imageName).append('\'');
        sb.append(", imageType='").append(imageType).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
