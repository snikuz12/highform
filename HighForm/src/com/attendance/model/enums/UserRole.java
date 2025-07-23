package com.attendance.model.enums;

public enum UserRole {
    STUDENT("학생"),
    PROFESSOR("교수"),
    MANAGER("관리자");
    
    private final String description;
    
    UserRole(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}