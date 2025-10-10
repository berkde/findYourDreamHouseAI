package com.dreamhouse.ai.authentication.model.entity;

import jakarta.persistence.*;

import java.io.Serial;
import java.io.Serializable;
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
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
        return authorities;
    }

    public void setAuthorities(Set<AuthorityEntity> authorities) {
        this.authorities = authorities;
    }
}
