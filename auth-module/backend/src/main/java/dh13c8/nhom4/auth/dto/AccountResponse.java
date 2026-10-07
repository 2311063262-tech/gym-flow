package dh13c8.nhom4.auth.dto;

import java.time.LocalDateTime;

/**
 * DTO response cho Account Management - hiển thị đầy đủ thông tin tài khoản cho admin.
 */
public class AccountResponse {
    private Long id;
    private String username;
    private String email;
    private String role;
    private String fullName;
    private String phone;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;

    public AccountResponse() {
    }

    public AccountResponse(Long id, String username, String email, String role, String fullName, 
                          String phone, Boolean isActive, LocalDateTime createdAt, LocalDateTime lastLoginAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.fullName = fullName;
        this.phone = phone;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.lastLoginAt = lastLoginAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }
}
