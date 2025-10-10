package com.dreamhouse.ai.authentication.model.request;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

public record RoleAuthorityEditRequestModel(@JsonProperty("role_name") String roleName,
                                            @JsonProperty("authorities") List<String> authorities,
                                            @JsonProperty("operation") String operation) {
    public RoleAuthorityEditRequestModel {
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RoleAuthorityEditRequestModel(String name, List<String> authorities1, String operation1))) return false;
        return Objects.equals(roleName, name) && Objects.equals(operation, operation1) && Objects.equals(authorities, authorities1);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roleName, authorities, operation);
    }
}
