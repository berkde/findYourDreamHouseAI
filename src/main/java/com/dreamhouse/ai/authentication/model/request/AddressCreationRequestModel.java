package com.dreamhouse.ai.authentication.model.request;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public record AddressCreationRequestModel(
     @JsonProperty("billing_street") String billingStreet,
     @JsonProperty("billing_city") String billingCity,
     @JsonProperty("billing_state") String billingState,
     @JsonProperty("billing_zip") String billingZip) {
    public AddressCreationRequestModel {
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AddressCreationRequestModel(String street, String city, String state, String zip))) return false;
        return Objects.equals(billingZip, zip) && Objects.equals(billingCity, city) && Objects.equals(billingState, state) && Objects.equals(billingStreet, street);
    }

    @Override
    public int hashCode() {
        return Objects.hash(billingStreet, billingCity, billingState, billingZip);
    }

}
