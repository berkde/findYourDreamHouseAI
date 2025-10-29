package com.dreamhouse.ai.authentication.model.entity;

import jakarta.persistence.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

@Entity
@Table(name = "roles")
public class RoleEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    @Version
    private Long version;

    @Column(unique = true, nullable = false)
    private String name;

    @ManyToMany(fetch = FetchType.EAGER, mappedBy = "roles", cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    private Set<UserEntity> users;

    @ManyToMany
    @JoinTable(
            name = "roles_authority",
            joinColumns = @JoinColumn(name = "roles_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "authorities_id", referencedColumnName = "id")
    )
    private Set<AuthorityEntity> authorities;


    public RoleEntity() {
        /*
            This constructor has been intentionally left empty for
            object marshalling and serialization motives
        */
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<UserEntity> getUsers() {
        return users;
    }

    public void setUsers(Set<UserEntity> users) {
        this.users = users;
    }

    public Set<AuthorityEntity> getAuthorities() {
        if (authorities == null) return Collections.emptySet();
        return authorities;
    }

    public void setAuthorities(Set<AuthorityEntity> authorities) {
        this.authorities = authorities;
    }
}
