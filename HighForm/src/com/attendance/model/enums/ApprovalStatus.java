package com.attendance.model.enums;

public enum ApprovalStatus {
    PROGRESSING("진행중"),
    APPROVE("승인"),
    REJECT("거절");
    
    private final String description;
    
    ApprovalStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}