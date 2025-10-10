package com.dreamhouse.ai.house.model.entity;

import com.dreamhouse.ai.authentication.model.entity.UserEntity;
import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Index;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

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

    @Column(columnDefinition = "text")
    private String description;

    @OneToMany(
            mappedBy = "houseAd",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private Set<HouseAdImageEntity> images = new LinkedHashSet<>();

    @OneToMany(
            mappedBy = "houseAd",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @OrderBy("messageDate DESC")
    private Set<HouseAdMessageEntity> messages = new LinkedHashSet<>();

    public HouseAdEntity() {}

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
