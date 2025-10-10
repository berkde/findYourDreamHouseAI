package com.dreamhouse.ai.house.model.entity;

import jakarta.persistence.*;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(
        name = "house_ad_images",
        uniqueConstraints = @UniqueConstraint(name = "uk_house_ad_image_uid",
                                              columnNames = "house_ad_image_uid"),
        indexes = @Index(name = "idx_house_ad_fk", columnList = "house_ad_fk")
)
public class HouseAdImageEntity implements Serializable {
    @Serial private static final long serialVersionUID = 1L;

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "house_ad_image_uid", unique = true, nullable = false, length = 64)
    private String houseAdImageUid;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "house_ad_fk", nullable = false)
    private HouseAdEntity houseAd;

    @Column(name = "image_url", nullable = false, length = 1024)
    private String imageUrl;

    @Column(name = "image_name", length = 255)
    private String imageName;

    @Column(name = "image_type", length = 100)
    private String imageType;

    @Column(name = "image_description", columnDefinition = "text")
    private String imageDescription;

    @Column(name = "image_thumbnail", length = 1024)
    private String imageThumbnail;

    @Column(name = "storage_key", length = 1024)
    private String storageKey;

    public HouseAdImageEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getHouseAdImageUid() { return houseAdImageUid; }
    public void setHouseAdImageUid(String houseAdImageUid) { this.houseAdImageUid = houseAdImageUid; }

    public HouseAdEntity getHouseAd() { return houseAd; }
    public void setHouseAd(HouseAdEntity houseAd) { this.houseAd = houseAd; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getImageName() { return imageName; }
    public void setImageName(String imageName) { this.imageName = imageName; }

    public String getImageType() { return imageType; }
    public void setImageType(String imageType) { this.imageType = imageType; }

    public String getImageDescription() { return imageDescription; }
    public void setImageDescription(String imageDescription) { this.imageDescription = imageDescription; }

    public String getImageThumbnail() { return imageThumbnail; }
    public void setImageThumbnail(String imageThumbnail) { this.imageThumbnail = imageThumbnail; }

    public String getStorageKey() {return storageKey;}

    public void setStorageKey(String storageKey) {this.storageKey = storageKey;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HouseAdImageEntity that)) return false;
        return Objects.equals(houseAdImageUid, that.houseAdImageUid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(houseAdImageUid);
    }

    @Override
    public String toString() {
        return "HouseAdImageEntity{" +
                "houseAdImageUid='" + houseAdImageUid + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", imageName='" + imageName + '\'' +
                ", imageType='" + imageType + '\'' +
                '}';
    }
}
