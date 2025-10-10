package com.dreamhouse.ai.authentication.controller;

import com.dreamhouse.ai.authentication.dto.UserDTO;
import com.dreamhouse.ai.authentication.model.request.AddressCreationRequestModel;
import com.dreamhouse.ai.authentication.service.impl.UserServiceImpl;
import com.dreamhouse.ai.authentication.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/user")
public class UserProfileController {
    private final UserServiceImpl userService;
    private final SecurityUtil securityUtil;
    private final static Logger log = LoggerFactory.getLogger(UserProfileController.class);

    @Autowired
    public UserProfileController(UserServiceImpl userService, SecurityUtil securityUtil) {
        this.userService = userService;
        this.securityUtil = securityUtil;
    }

    @GetMapping(path = "/{userId}")
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_ADMIN')")
    public ResponseEntity<UserDTO> getUserProfile(@PathVariable("userId") String userId,
                                                  @AuthenticationPrincipal String username) {
        if (!securityUtil.isUserRequestValid(userId, username)) {
            return ResponseEntity.badRequest().body(null);
        }

        log.info("getUserProfile - Getting user profile for user: {}", userId);
        var userProfile = userService.getUserById(userId);
        return ResponseEntity.ok(userProfile);
    }

    @PostMapping("/{userId}/address")
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_ADMIN')")
    public ResponseEntity<String> addOrUpdateBillingAddress(@PathVariable("userId") String userId,
                                                             @RequestBody AddressCreationRequestModel model,
                                                             @AuthenticationPrincipal String username) {
        if (!securityUtil.isUserRequestValid(userId, username)) {
            return ResponseEntity.badRequest().body(null);
        }

        log.info("addBillingAndShippingAddress - Adding billing and shipping address");
        var isAddressAdded = userService.addOrUpdateBillingAddress(userId, model);
        return isAddressAdded ? ResponseEntity.ok("Address added/updated") : ResponseEntity.badRequest().body("Address not added/updated");
    }
}
