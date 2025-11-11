package com.dreamhouse.ai.authentication.dto;

import java.util.Objects;

public class AddressDTO {
    private String addressID;
    private String billingStreet;
    private String billingCity;
    private String billingState;
    private String billingZip;

    public AddressDTO() {
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

    public String getBillingStreet() {
        return billingStreet;
    }

    public void setBillingStreet(String billingStreet) {
        this.billingStreet = billingStreet;
    }

    public String getBillingCity() {
        return billingCity;
    }

    public void setBillingCity(String billingCity) {
        this.billingCity = billingCity;
    }

    public String getBillingState() {
        return billingState;
    }

    public void setBillingState(String billingState) {
        this.billingState = billingState;
    }

    public String getBillingZip() {
        return billingZip;
    }

    public void setBillingZip(String billingZip) {
        this.billingZip = billingZip;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AddressDTO that)) return false;
        return Objects.equals(addressID, that.addressID) && Objects.equals(billingStreet, that.billingStreet) && Objects.equals(billingCity, that.billingCity) && Objects.equals(billingState, that.billingState) && Objects.equals(billingZip, that.billingZip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(addressID, billingStreet, billingCity, billingState, billingZip);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AddressDTO{");
        sb.append("addressID='").append(addressID).append('\'');
        sb.append(", billingStreet='").append(billingStreet).append('\'');
        sb.append(", billingCity='").append(billingCity).append('\'');
        sb.append(", billingState='").append(billingState).append('\'');
        sb.append(", billingZip='").append(billingZip).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
