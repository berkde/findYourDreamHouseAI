package com.dreamhouse.ai.authentication.dto;


import java.util.Objects;

public class UserDTO {
    private String userID;
    private String username;
    private String name;
    private String lastname;
    private AddressDTO billingAddress;
    private String type;
    private String aiAuthToken;

    public UserDTO() {
        /*
            This constructor has been intentionally left empty for
            object marshalling and serialization motives
        */
    }

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

    public AddressDTO getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(AddressDTO billingAddress) {
        this.billingAddress = billingAddress;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAiAuthToken() {
        return aiAuthToken;
    }

    public void setAiAuthToken(String aiAuthToken) {
        this.aiAuthToken = aiAuthToken;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UserDTO userDTO)) return false;
        return Objects.equals(userID, userDTO.userID) && Objects.equals(username, userDTO.username) && Objects.equals(name, userDTO.name) && Objects.equals(lastname, userDTO.lastname) && Objects.equals(billingAddress, userDTO.billingAddress) && Objects.equals(type, userDTO.type) && Objects.equals(aiAuthToken, userDTO.aiAuthToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userID, username, name, lastname, billingAddress, type, aiAuthToken);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UserDTO{");
        sb.append("userID='").append(userID).append('\'');
        sb.append(", username='").append(username).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", lastname='").append(lastname).append('\'');
        sb.append(", billingAddress=").append(billingAddress);
        sb.append(", type='").append(type).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
