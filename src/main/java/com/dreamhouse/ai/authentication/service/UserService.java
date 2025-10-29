package com.dreamhouse.ai.authentication.service;

import com.dreamhouse.ai.authentication.dto.UserDTO;
import com.dreamhouse.ai.authentication.model.request.AddressCreationRequestModel;
import com.dreamhouse.ai.authentication.model.request.RoleAuthorityEditRequestModel;
import com.dreamhouse.ai.authentication.model.request.UserRegisterRequest;
import com.dreamhouse.ai.authentication.model.response.UserRegisterResponse;

public interface UserService {
    /**
     * Retrieves a user by their username.
     * @param username the username to search for
     * @return UserDTO containing user information
     */
    UserDTO getUserByUsername(String username);
    
    /**
     * Retrieves a user by their unique identifier.
     * @param userId the unique user identifier
     * @return UserDTO containing user information
     */
    UserDTO getUserById(String userId);
    
    /**
     * Registers a new user in the system.
     * @param userRegisterRequest the user registration request containing user details
     * @return UserRegisterResponse containing the created user information
     */
    UserRegisterResponse registerUser(UserRegisterRequest userRegisterRequest);
    
    /**
     * Edits role authorities by adding or removing permissions.
     * @param roleAuthorityEditRequest the request containing role name, operation, and authorities
     * @return Boolean indicating success of the operation
     */
    Boolean editRoleAuthorities(RoleAuthorityEditRequestModel roleAuthorityEditRequest);
    
    /**
     * Deletes a user account and all associated data.
     * @param userId the unique identifier of the user to delete
     */
    void deleteAccount(String userId);
    
    /**
     * Adds or updates the billing address for a user.
     * @param userId the unique identifier of the user
     * @param model the address creation request model containing address details
     * @return Boolean indicating success of the operation
     */
    Boolean addOrUpdateBillingAddress(String userId, AddressCreationRequestModel model);
}
