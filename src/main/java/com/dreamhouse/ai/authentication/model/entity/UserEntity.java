package com.dreamhouse.ai.authentication.model.entity;

import com.dreamhouse.ai.house.model.entity.HouseAdEntity;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Table;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Index;
import jakarta.persistence.OneToOne;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;


import java.io.Serial;
import java.io.Serializable;
import java.util.*;

@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "users",
        indexes = {
                @Index(name = "idx_users_userid", columnList = "users_id"),
                @Index(name = "idx_users_username", columnList = "username"),
                @Index(name = "idx_users_auth_token", columnList = "authorization_token"),
        })
public class UserEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    @Column(name = "users_id", unique = true, nullable = false)
    private String userID;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false) private String name;
    @Column(nullable = false) private String lastname;
    @Column(nullable = false) private String password;
    @Column(name = "authorization_token", unique = true, length = 512) private String authorizationToken;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_login")
    private Date lastLogin;

    @Column(name = "is_deleted") private boolean isDeleted;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_password_update")
    private Date lastPasswordUpdate;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "billing_address_id", unique = true)
    private AddressEntity billingAddress;

    @ManyToMany
    @JoinTable(
            name = "users_role",
            joinColumns = @JoinColumn(name = "users_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "roles_id", referencedColumnName = "id")
    )
    private Set<RoleEntity> roles;


    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<HouseAdEntity> houseAds = new ArrayList<>();

    public UserEntity() {
    }

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<RoleEntity> getRoles() {
        return roles;
    }

    public void setRoles(Set<RoleEntity> roles) {
        this.roles = roles;
    }


    public String getAuthorizationToken() {
        return authorizationToken;
    }

    public void setAuthorizationToken(String authorizationToken) {
        this.authorizationToken = authorizationToken;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public Date getLastPasswordUpdate() {
        return lastPasswordUpdate;
    }

    public void setLastPasswordUpdate(Date lastPasswordUpdate) {
        this.lastPasswordUpdate = lastPasswordUpdate;
    }

    public AddressEntity getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(AddressEntity billingAddress) {
        this.billingAddress = billingAddress;
    }


    public void addHouseAd(HouseAdEntity houseAd) {
        if (houseAd == null) return;
        if (!houseAds.contains(houseAd)) {
            houseAds.add(houseAd);
            houseAd.setUser(this);
        }
    }

    public void removeHouseAd(HouseAdEntity houseAd) {
        if (houseAd == null) return;
        if (houseAds.remove(houseAd)) {
            houseAd.setUser(null);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserEntity that)) return false;
        return Objects.equals(userID, that.userID);
    }

    @Override
    public int hashCode() { return Objects.hash(userID); }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UserEntity{");
        sb.append("userID='").append(userID).append('\'');
        sb.append(", username='").append(username).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", lastname='").append(lastname).append('\'');
        sb.append(", lastLogin=").append(lastLogin);
        sb.append(", isDeleted=").append(isDeleted);
        sb.append(", lastPasswordUpdate=").append(lastPasswordUpdate);
        sb.append('}');
        return sb.toString();
    }
}
