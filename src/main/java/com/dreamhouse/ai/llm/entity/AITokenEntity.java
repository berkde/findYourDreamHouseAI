package com.dreamhouse.ai.llm.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "ai_tokens")
public class AITokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String token;

    @Column(nullable = false)
    private String planCode;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private int monthlyQuota;

    @Column(name = "expiry_date", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expiryDate;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    public AITokenEntity() {}


    public void extendIfExpired() {
        if (expiryDate.isBefore(LocalDateTime.now())) {
            this.expiryDate = expiryDate.plusMonths(1L);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPlanCode() {
        return planCode;
    }

    public void setPlanCode(String planCode) {
        this.planCode = planCode;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getMonthlyQuota() {
        return monthlyQuota;
    }

    public void setMonthlyQuota() {
        switch (this.planCode) {
            case "freemium" -> this.monthlyQuota = 12;
            case "basic" -> this.monthlyQuota = 300;
            case "premium" -> this.monthlyQuota = 600;
            case "unlimited" -> this.monthlyQuota = 1200;
        }
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate() {
        this.expiryDate = LocalDateTime.now().plusMonths(1);
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AITokenEntity that)) return false;
        return Objects.equals(id, that.id) && Objects.equals(token, that.token) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, token, userId);
    }
}