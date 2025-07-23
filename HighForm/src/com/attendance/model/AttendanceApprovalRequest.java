package com.attendance.model;

import com.attendance.model.enums.ApprovalStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class AttendanceApprovalRequest {
    private Long id;
    private String reason;               // 사유
    private String proofFile;           // 증빙 파일
    private ApprovalStatus status;      // 승인 상태
    private LocalDateTime requestedAt;  // 요청 시간
    private LocalDateTime decisionAt;   // 결정 시간
    private LocalDate startDate;        // 시작 날짜
    private LocalDate endDate;          // 종료 날짜
    private Long userId;                // 요청자 ID
    private Long approverId;            // 승인자 ID
    
    // 기본 생성자
    public AttendanceApprovalRequest() {}
    
    // 생성자 (요청 시 사용)
    public AttendanceApprovalRequest(Long userId, String reason, LocalDate startDate, LocalDate endDate) {
        this.userId = userId;
        this.reason = reason;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = ApprovalStatus.PROGRESSING;
        this.requestedAt = LocalDateTime.now();
    }
    
    // 생성자 (파일 포함)
    public AttendanceApprovalRequest(Long userId, String reason, LocalDate startDate, LocalDate endDate, String proofFile) {
        this(userId, reason, startDate, endDate);
        this.proofFile = proofFile;
    }
    
    // Getter & Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    
    public String getProofFile() { return proofFile; }
    public void setProofFile(String proofFile) { this.proofFile = proofFile; }
    
    public ApprovalStatus getStatus() { return status; }
    public void setStatus(ApprovalStatus status) { this.status = status; }
    
    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }
    
    public LocalDateTime getDecisionAt() { return decisionAt; }
    public void setDecisionAt(LocalDateTime decisionAt) { this.decisionAt = decisionAt; }
    
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public Long getApproverId() { return approverId; }
    public void setApproverId(Long approverId) { this.approverId = approverId; }
    
    // 편의 메서드들
    
    /**
     * 승인 처리
     */
    public void approve(Long approverId) {
        this.status = ApprovalStatus.APPROVE;
        this.approverId = approverId;
        this.decisionAt = LocalDateTime.now();
    }
    
    /**
     * 거절 처리
     */
    public void reject(Long approverId) {
        this.status = ApprovalStatus.REJECT;
        this.approverId = approverId;
        this.decisionAt = LocalDateTime.now();
    }
    
    /**
     * 특정 날짜가 승인 기간에 포함되는지 확인
     */
    public boolean includesDate(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }
    
    /**
     * 승인된 요청인지 확인
     */
    public boolean isApproved() {
        return this.status == ApprovalStatus.APPROVE;
    }
    
    /**
     * 진행 중인 요청인지 확인
     */
    public boolean isPending() {
        return this.status == ApprovalStatus.PROGRESSING;
    }
    
    /**
     * 거절된 요청인지 확인
     */
    public boolean isRejected() {
        return this.status == ApprovalStatus.REJECT;
    }
    
    // equals & hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AttendanceApprovalRequest that = (AttendanceApprovalRequest) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "AttendanceApprovalRequest{" +
                "id=" + id +
                ", reason='" + reason + '\'' +
                ", status=" + status +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", userId=" + userId +
                ", approverId=" + approverId +
                '}';
    }
}