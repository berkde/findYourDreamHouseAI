package com.dreamhouse.ai.authentication.model.security;

import com.dreamhouse.ai.authentication.model.entity.AuthorityEntity;
import com.dreamhouse.ai.authentication.model.entity.RoleEntity;
import com.dreamhouse.ai.authentication.repository.AuthorityRepository;
import com.dreamhouse.ai.authentication.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Component
public class RoleAndAuthorityDataSeeder implements CommandLineRunner {
    private final RoleRepository roleRepository;
    private final AuthorityRepository authorityRepository;

    @Autowired
    public RoleAndAuthorityDataSeeder(RoleRepository roleRepository, AuthorityRepository authorityRepository) {
        this.roleRepository = roleRepository;
        this.authorityRepository = authorityRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        AuthorityEntity read   = ensureAuthority("READ");
        AuthorityEntity write  = ensureAuthority("WRITE");
        AuthorityEntity delete = ensureAuthority("DELETE");

        ensureRole("ROLE_USER",  new HashSet<>(Set.of(read, write)));
        ensureRole("ROLE_ADMIN", new HashSet<>(Set.of(read, write, delete)));
    }

    private AuthorityEntity ensureAuthority(String name) {
        return authorityRepository.findByName(name)
                .orElseGet(() -> {
                    AuthorityEntity a = new AuthorityEntity();
                    a.setName(name);
                    return authorityRepository.save(a);
                });
    }

    private void ensureRole(String name, Set<AuthorityEntity> desiredAuthorities) {
        RoleEntity role = roleRepository.findByName(name)
                .orElseGet(() -> {
                    RoleEntity r = new RoleEntity();
                    r.setName(name);
                    return r;
                });

        if (role.getAuthorities() == null ||
                !role.getAuthorities().containsAll(desiredAuthorities) ||
                !desiredAuthorities.containsAll(role.getAuthorities())) {
            role.setAuthorities(new HashSet<>(desiredAuthorities));
        }

        roleRepository.save(role);
    }
}
