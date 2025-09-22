package com.attendance.model;

import com.attendance.model.enums.UserRole;
import java.time.LocalDateTime;
import java.util.Objects;

public class UserInfo {
    private Long id;
    private String loginId;
    private String password;
    private String phone;
    private String email;
    private String name;
    private UserRole role;
    private LocalDateTime createdAt;
    private String delYn;
    
    // 기본 생성자
    public UserInfo() {}
    
    // 생성자
    public UserInfo(String loginId, String password, String name, UserRole role, String email, String phone) {
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.role = role;
        this.email = email;
        this.phone = phone;
        this.delYn = "N";
        this.createdAt = LocalDateTime.now();
    }
    
    // Getter & Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getLoginId() { return loginId; }
    public void setLoginId(String loginId) { this.loginId = loginId; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public String getDelYn() { return delYn; }
    public void setDelYn(String delYn) { this.delYn = delYn; }
    
    // 편의 메서드
    public boolean isDeleted() {
        return "Y".equals(this.delYn);
    }
    
    public void delete() {
        this.delYn = "Y";
    }
    
    // equals & hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserInfo userInfo = (UserInfo) o;
        return Objects.equals(id, userInfo.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "UserInfo{" +
                "id=" + id +
                ", loginId='" + loginId + '\'' +
                ", name='" + name + '\'' +
                ", role=" + role +
                ", email='" + email + '\'' +
                ", delYn='" + delYn + '\'' +
                '}';
    }
}