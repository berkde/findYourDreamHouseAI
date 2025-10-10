package com.dreamhouse.ai.authentication.service;

import com.dreamhouse.ai.authentication.dto.UserDTO;
import com.dreamhouse.ai.authentication.model.request.AddressCreationRequestModel;
import com.dreamhouse.ai.authentication.model.request.RoleAuthorityEditRequestModel;
import com.dreamhouse.ai.authentication.model.request.UserRegisterRequest;
import com.dreamhouse.ai.authentication.model.response.UserRegisterResponse;

public interface UserService {
    UserDTO getUserByUsername(String username);
    UserDTO getUserById(String userId);
    UserRegisterResponse registerUser(UserRegisterRequest userRegisterRequest);
    Boolean editRoleAuthorities(RoleAuthorityEditRequestModel roleAuthorityEditRequest);
    Boolean deleteAccount(String userId);
    Boolean addOrUpdateBillingAddress(String userId, AddressCreationRequestModel model);
}
