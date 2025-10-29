package com.dreamhouse.ai.house.dto;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class HouseAdDTO {
    private String houseAdUid;
    private String title;
    private String description;
    private String city;
    private String viewUrl;
    private List<HouseAdImageDTO> images;

    public HouseAdDTO() {
    }

    public String getHouseAdUid() { return houseAdUid; }
    public void setHouseAdUid(String houseAdUid) { this.houseAdUid = houseAdUid; }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getViewUrl() {
        return viewUrl;
    }

    public void setViewUrl(String viewUrl) {
        this.viewUrl = viewUrl;
    }

    public List<HouseAdImageDTO> getImages() {
        if (images == null) images = Collections.emptyList();
        return List.copyOf(images);
    }

    public void setImages(List<HouseAdImageDTO> images) {
        this.images = images;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof HouseAdDTO that)) return false;
        return Objects.equals(houseAdUid, that.houseAdUid);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(houseAdUid);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("HouseAdDTO{");
        sb.append("houseAdUid='").append(houseAdUid).append('\'');
        sb.append(", title='").append(title).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", city='").append(city).append('\'');
        sb.append(", viewUrl='").append(viewUrl).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
