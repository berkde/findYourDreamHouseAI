package com.dreamhouse.ai.house.model.entity;

import com.dreamhouse.ai.authentication.model.entity.UserEntity;
import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Index;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import org.hibernate.annotations.*;
import org.hibernate.annotations.Cache;
import org.hibernate.type.SqlTypes;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(
        name = "house_ads",
        uniqueConstraints = @UniqueConstraint(name = "uk_house_ad_uid", columnNames = "house_ad_uid"),
        indexes = @Index(name = "idx_house_ads_user_fk", columnList = "user_id")
)
public class HouseAdEntity implements Serializable {
    @Serial private static final long serialVersionUID = 1L;

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "house_ad_uid", nullable = false, unique = true, length = 64)
    private String houseAdUid;
    
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private UserEntity user;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "text", length = 10000)
    private String description;

    private String city;

    private String state;

    private String neighborhood;

    private Double price;

    private Integer beds;

    private Integer baths;

    private Integer sqft;

    private String type;

    private Boolean parking;

    private Boolean petsAllowed;

    private Boolean waterfront;

    private Integer yearBuilt;

    @JdbcTypeCode(SqlTypes.VECTOR)
    @Array(length = 768)
    @Column(columnDefinition = "vector(768)")
    private float[] embedding;

    @OneToMany(
            mappedBy = "houseAd",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @BatchSize(size = 50)
    private Set<HouseAdImageEntity> images = new HashSet<>();



    @OneToMany(
            mappedBy = "houseAd",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @OrderBy("messageDate DESC")
    private Set<HouseAdMessageEntity> messages = new LinkedHashSet<>();

    public HouseAdEntity() {}

    public HouseAdEntity(String houseAdUid,
                         UserEntity user,
                         String title,
                         String description,
                         String city,
                         String state,
                         String neighborhood,
                         Double price,
                         Integer beds,
                         Integer baths,
                         Integer sqft,
                         String type,
                         Boolean parking,
                         Boolean petsAllowed,
                         Boolean waterfront,
                         Integer yearBuilt,
                         float[] embedding,
                         Set<HouseAdImageEntity> images,
                         Set<HouseAdMessageEntity> messages) {
        this.houseAdUid = houseAdUid;
        this.user = user;
        this.title = title;
        this.description = description;
        this.city = city;
        this.state = state;
        this.neighborhood = neighborhood;
        this.price = price;
        this.beds = beds;
        this.baths = baths;
        this.sqft = sqft;
        this.type = type;
        this.parking = parking;
        this.petsAllowed = petsAllowed;
        this.waterfront = waterfront;
        this.yearBuilt = yearBuilt;
        this.embedding = embedding;
        this.images = images;
        this.messages = messages;
    }

    public void addImage(HouseAdImageEntity image) {
        if (image == null) return;
        if (!images.contains(image)) {
            images.add(image);
            image.setHouseAd(this);
        }
    }
    public void removeImage(HouseAdImageEntity image) {
        if (image == null || images == null) return;
        if (images.remove(image)) image.setHouseAd(null);
    }

    public void addMessage(HouseAdMessageEntity message) {
        if (message == null) return;
        if (!messages.contains(message)) {
            messages.add(message);
            message.setHouseAd(this);
        }
    }

    public void removeMessage(HouseAdMessageEntity message) {
        if (message == null) return;
        if (messages.remove(message)) {
            message.setHouseAd(null);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getHouseAdUid() { return houseAdUid; }
    public void setHouseAdUid(String houseAdUid) { this.houseAdUid = houseAdUid; }
    
    public UserEntity getUser() { return user; }
    public void setUser(UserEntity user) { this.user = user; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public void setNeighborhood(String neighborhood) {
        this.neighborhood = neighborhood;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getBeds() {
        return beds;
    }

    public void setBeds(Integer beds) {
        this.beds = beds;
    }

    public Integer getBaths() {
        return baths;
    }

    public void setBaths(Integer baths) {
        this.baths = baths;
    }

    public Integer getSqft() {
        return sqft;
    }

    public void setSqft(Integer sqft) {
        this.sqft = sqft;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getParking() {
        return parking;
    }

    public void setParking(Boolean parking) {
        this.parking = parking;
    }

    public Boolean getPetsAllowed() {
        return petsAllowed;
    }

    public void setPetsAllowed(Boolean petsAllowed) {
        this.petsAllowed = petsAllowed;
    }

    public Boolean getWaterfront() {
        return waterfront;
    }

    public void setWaterfront(Boolean waterfront) {
        this.waterfront = waterfront;
    }

    public Integer getYearBuilt() {
        return yearBuilt;
    }

    public void setYearBuilt(Integer yearBuilt) {
        this.yearBuilt = yearBuilt;
    }

    public float[] getEmbedding() {
        return embedding;
    }

    public void setEmbedding(float[] embedding) {
        this.embedding = embedding;
    }

    public Set<HouseAdImageEntity> getImages() { return images; }
    public void setImages(Set<HouseAdImageEntity> images) { this.images = images; }

    public Set<HouseAdMessageEntity> getMessages() {
        return messages;
    }

    public void setMessages(Set<HouseAdMessageEntity> messages) {
        this.messages = messages;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HouseAdEntity that)) return false;
        return Objects.equals(houseAdUid, that.houseAdUid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(houseAdUid);
    }

    @Override
    public String toString() {
        return "HouseAdEntity{" +
                "houseAdUid='" + houseAdUid + '\'' +
                ", title='" + title + '\'' +
                ", imagesCount=" + (images != null ? images.size() : 0) +
                '}';
    }
}
