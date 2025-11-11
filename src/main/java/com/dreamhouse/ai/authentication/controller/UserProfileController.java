package com.dreamhouse.ai.authentication.controller;

import com.dreamhouse.ai.authentication.dto.UserDTO;
import com.dreamhouse.ai.authentication.model.request.AddressCreationRequestModel;
import com.dreamhouse.ai.authentication.service.impl.UserServiceImpl;
import com.dreamhouse.ai.authentication.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/user")
@Validated
public class UserProfileController {
    private final UserServiceImpl userService;
    private final SecurityUtil securityUtil;
    private static final Logger log = LoggerFactory.getLogger(UserProfileController.class);

    @Autowired
    public UserProfileController(UserServiceImpl userService, SecurityUtil securityUtil) {
        this.userService = userService;
        this.securityUtil = securityUtil;
    }

    @WriteOperation
    @PostMapping("/{userId}/address")
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_ADMIN')")
    public ResponseEntity<String> addOrUpdateBillingAddress(@PathVariable("userId") String userId,
                                                            @RequestBody AddressCreationRequestModel model) {
        var username = securityUtil.getAuthenticatedUser();
        if (securityUtil.isUserRequestValid(userId, username) == Boolean.FALSE) {
            return ResponseEntity.badRequest().body(null);
        }

        log.info("Adding billing and shipping address");
        var isAddressAdded = userService.addOrUpdateBillingAddress(userId, model);
        return isAddressAdded == Boolean.TRUE ? ResponseEntity.ok("Address added/updated") :
                ResponseEntity.badRequest().body("Address not added/updated");
    }

    @ReadOperation
    @GetMapping(path = "/{userId}")
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_ADMIN')")
    public ResponseEntity<UserDTO> getUserProfile(@PathVariable("userId") String userId) {
        var username = securityUtil.getAuthenticatedUser();
        if (securityUtil.isUserRequestValid(userId, username) == Boolean.FALSE) {
            return ResponseEntity.badRequest().body(null);
        }

        log.info("Getting user profile for user");
        var userProfile = userService.getUserById(userId);
        return ResponseEntity.ok(userProfile);
    }

}
