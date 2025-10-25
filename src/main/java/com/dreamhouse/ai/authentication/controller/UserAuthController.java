package com.dreamhouse.ai.authentication.controller;

import com.dreamhouse.ai.authentication.model.request.RoleAuthorityEditRequestModel;
import com.dreamhouse.ai.authentication.model.request.UserRegisterRequest;
import com.dreamhouse.ai.authentication.model.response.UserRegisterResponse;
import com.dreamhouse.ai.authentication.service.impl.UserServiceImpl;
import com.dreamhouse.ai.authentication.util.SecurityUtil;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/auth")
public class UserAuthController {
    private final UserServiceImpl userService;
    private final SecurityUtil securityUtil;
    private static final Logger log = LoggerFactory.getLogger(UserAuthController.class);

    @Autowired
    public UserAuthController(UserServiceImpl userService, SecurityUtil securityUtil) {
        this.userService = userService;
        this.securityUtil = securityUtil;
    }

    @PostMapping(path = "/register")
    @PermitAll
    public ResponseEntity<UserRegisterResponse> registerUser(@Valid @RequestBody UserRegisterRequest registerRequest) {
        log.info("Registering user");
        UserRegisterResponse registeredUser = userService.registerUser(registerRequest);
        return ResponseEntity.ok(registeredUser);
    }

    @DeleteMapping("/account-deletion/{userId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    public ResponseEntity<String> deleteAccount(@PathVariable("userId") String userId,
                                                @AuthenticationPrincipal String authenticatedUsername) {
        if (securityUtil.isUserRequestValid(userId, authenticatedUsername) == Boolean.FALSE) {
            return ResponseEntity.badRequest().body(null);
        }

        log.info("Deleting account");
        var isAccountDeleted = userService.deleteAccount(userId);
        return isAccountDeleted == Boolean.TRUE ? ResponseEntity.ok("Account deleted") :
                                                  ResponseEntity.badRequest().body("Account not deleted");
    }

    @PostMapping("/authority/edit")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Boolean> editRoleAuthorities(@RequestBody RoleAuthorityEditRequestModel roleAuthorityEditRequest) {
        log.info("Editing role authorities");
        return ResponseEntity.ok(userService.editRoleAuthorities(roleAuthorityEditRequest));
    }
}
