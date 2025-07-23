package com.attendance.model;

import com.attendance.model.enums.AttendanceStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.Objects;

public class Attendance {
    private Long id;
    private LocalDate attendanceDate;    // 출석 날짜 (배치 처리용)
    private LocalDateTime checkIn;       // 출석 시간
    private LocalDateTime checkOut;      // 퇴실 시간
    private AttendanceStatus status;     // 출석 상태
    private Long userId;                 // 사용자 ID
    private LocalDateTime createdAt;     // 생성 시간
    private LocalDateTime updatedAt;     // 수정 시간
    
    // 기본 생성자
    public Attendance() {}
    
    // 생성자 (출석 시 사용)
    public Attendance(Long userId, LocalDate attendanceDate, LocalDateTime checkIn, AttendanceStatus status) {
        this.userId = userId;
        this.attendanceDate = attendanceDate;
        this.checkIn = checkIn;
        this.status = status;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getter & Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public LocalDate getAttendanceDate() { return attendanceDate; }
    public void setAttendanceDate(LocalDate attendanceDate) { this.attendanceDate = attendanceDate; }
    
    public LocalDateTime getCheckIn() { return checkIn; }
    public void setCheckIn(LocalDateTime checkIn) { this.checkIn = checkIn; }
    
    public LocalDateTime getCheckOut() { return checkOut; }
    public void setCheckOut(LocalDateTime checkOut) { this.checkOut = checkOut; }
    
    public AttendanceStatus getStatus() { return status; }
    public void setStatus(AttendanceStatus status) { this.status = status; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // 편의 메서드들 (출석 로직용)
    
    /**
     * 퇴실 처리가 가능한지 확인
     */
    public boolean canCheckOut() {
        return this.checkIn != null && this.checkOut == null;
    }
    
    /**
     * 근무 시간 계산 (시간 단위)
     */
    public double getWorkingHours() {
        if (checkIn == null || checkOut == null) {
            return 0.0;
        }
        Duration duration = Duration.between(checkIn, checkOut);
        return duration.toMinutes() / 60.0;
    }
    
    /**
     * 퇴실 처리 및 상태 업데이트
     */
    public void processCheckOut(LocalDateTime checkOutTime) {
        this.checkOut = checkOutTime;
        this.updatedAt = LocalDateTime.now();
        
        // 승인된 휴가/병가는 상태 변경 없음
        if (this.status == AttendanceStatus.EXCUSED) {
            return;
        }
        
        // 4시간 미만 근무 시 결석 처리
        if (getWorkingHours() < 4.0) {
            this.status = AttendanceStatus.ABSENT;
        }
        // 4시간 이상 근무 시 기존 상태 유지 (PRESENT 또는 LATE)
    }
    
    /**
     * 자동 결석 처리 (23:00에 배치로 실행)
     */
    public void processAutoAbsent(LocalDateTime midnightTime) {
        if (this.status == AttendanceStatus.EXCUSED) {
            return; // 승인된 휴가/병가는 처리하지 않음
        }
        
        this.checkOut = midnightTime;
        this.status = AttendanceStatus.ABSENT;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 출석 여부 확인
     */
    public boolean isCheckedIn() {
        return this.checkIn != null;
    }
    
    /**
     * 퇴실 여부 확인
     */
    public boolean isCheckedOut() {
        return this.checkOut != null;
    }
    
    // equals & hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Attendance that = (Attendance) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Attendance{" +
                "id=" + id +
                ", attendanceDate=" + attendanceDate +
                ", checkIn=" + checkIn +
                ", checkOut=" + checkOut +
                ", status=" + status +
                ", userId=" + userId +
                '}';
    }
}