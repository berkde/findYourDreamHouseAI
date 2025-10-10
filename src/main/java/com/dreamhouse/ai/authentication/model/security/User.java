package com.dreamhouse.ai.authentication.model.security;

import com.dreamhouse.ai.authentication.model.entity.AuthorityEntity;
import com.dreamhouse.ai.authentication.model.entity.UserEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import static java.util.stream.Collectors.toList;

public class User implements UserDetails {
    private static final Logger log = LoggerFactory.getLogger(User.class);
    private static final long ACCOUNT_EXPIRY_MONTHS = 6;
    private static final long CREDENTIAL_EXPIRY_MONTHS = 3;
    private final UserEntity userEntity;

    public User(UserEntity userEntity) {
        this.userEntity = userEntity;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        var roles = userEntity.getRoles();
        if (roles == null || roles.isEmpty()) {
            log.warn("User {} has no roles assigned", userEntity.getUsername());
            return Collections.emptyList();
        }

        var authorities = roles.stream()
                .flatMap(role -> {
                    var stream = new ArrayList<String>();

                    stream.add(role.getName());
                    if (role.getAuthorities() != null) {
                        role.getAuthorities().stream()
                                .map(AuthorityEntity::getName)
                                .forEach(stream::add);
                    }
                    return stream.stream();
                })
                .distinct()
                .map(SimpleGrantedAuthority::new)
                .collect(toList());

        System.out.println("Authorities: " + authorities);

        if (authorities.isEmpty()) {
            log.warn("User {} has no valid authorities", userEntity.getUsername());
        }
        
        return authorities;
    }

    @Override
    public String getPassword() {
        return userEntity.getPassword();
    }

    @Override
    public String getUsername() {
        return userEntity.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        Instant cutoff = ZonedDateTime.now(ZoneOffset.UTC).minusMonths(ACCOUNT_EXPIRY_MONTHS).toInstant();

        Date lastLogin = userEntity.getLastLogin();
        Date fallback = userEntity.getLastPasswordUpdate();

        Instant lastActivity =
                lastLogin != null ? lastLogin.toInstant() :
                        (fallback != null ? fallback.toInstant() : null);

        if (lastActivity == null) return true;

        return lastActivity.isAfter(cutoff);
    }

    @Override
    public boolean isAccountNonLocked() {
        return Boolean.TRUE;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        Date lastPassword = userEntity.getLastPasswordUpdate();
        if (lastPassword == null) return Boolean.TRUE;
        Instant cutoff = ZonedDateTime.now(ZoneOffset.UTC).minusMonths(CREDENTIAL_EXPIRY_MONTHS).toInstant();
        return lastPassword.toInstant().isAfter(cutoff);
    }

    @Override
    public boolean isEnabled() {
        return !userEntity.isDeleted();
    }
}
