package com.dreamhouse.ai.authentication.model.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;


@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "addresses",
        indexes = {
                @Index(name = "idx_addresses_address_id", columnList = "address_id"),
                @Index(name = "idx_addresses_zip", columnList = "zip"),
                @Index(name = "idx_addresses_city", columnList = "city"),
                @Index(name = "idx_addresses_state", columnList = "state"),
                @Index(name = "idx_addresses_city_state", columnList = "city,state")
        },
        uniqueConstraints = @UniqueConstraint(
                name = "uniq_addr_street_city_state_zip",
                columnNames = {"street","city","state","zip"}
        ))
public class AddressEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "address_id", unique = true)
    private String addressID;

    @Version
    private Long version;

    @Column(nullable = false) private String street;
    @Column(nullable = false) private String city;
    @Column(nullable = false) private String state;
    @Column(nullable = false) private String zip;

    @OneToOne(mappedBy = "billingAddress")
    private UserEntity billedUser;

    public AddressEntity() {
        /*
            This constructor has been intentionally left empty for
            object marshalling and serialization motives
        */
    }

    public String getAddressID() {
        return addressID;
    }

    public void setAddressID(String addressID) {
        this.addressID = addressID;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

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

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public UserEntity getBilledUser() {
        return billedUser;
    }

    public void setBilledUser(UserEntity billedUser) {
        this.billedUser = billedUser;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AddressEntity address)) return false;
        return Objects.equals(addressID, address.addressID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(addressID);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Address{");
        sb.append("addressID='").append(addressID).append('\'');
        sb.append(", street='").append(street).append('\'');
        sb.append(", city='").append(city).append('\'');
        sb.append(", state='").append(state).append('\'');
        sb.append(", zip='").append(zip).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
