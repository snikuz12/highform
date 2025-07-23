package com.mypage.Model.attendance;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 출석 정보를 담는 VO 클래스
 * attendance 테이블과 매핑
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendance {
    
    private Long id;                    // 출석 ID (PK)
    private LocalDate attendanceDate;   // 출석 날짜 (YYYY-MM-DD)
    private LocalDateTime checkIn;      // 출석 시간 (날짜+시간)
    private LocalDateTime checkOut;     // 퇴실 시간 (날짜+시간)
    private AttendanceStatus status;    // 출석 상태 (ENUM)
    private Long userId;                // 사용자 ID (FK)
    private LocalDateTime createdAt;    // 생성 시간
    private LocalDateTime updatedAt;    // 수정 시간
    
    // 출석 상태를 나타내는 ENUM
    public enum AttendanceStatus {
        PRESENT("PRESENT", "정상출석"),
        LATE("LATE", "지각"),
        ABSENT("ABSENT", "결석"),
        EXCUSED("EXCUSED", "승인된 휴가/병가"),
        EARLY_LEAVE("EARLY_LEAVE", "조퇴");
        
        private final String code;
        private final String description;
        
        AttendanceStatus(String code, String description) {
            this.code = code;
            this.description = description;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getDescription() {
            return description;
        }
        
        // String 코드로부터 ENUM 찾기
        public static AttendanceStatus fromCode(String code) {
            for (AttendanceStatus status : values()) {
                if (status.code.equals(code)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Invalid attendance status code: " + code);
        }
    }
    

    
    // 생성자 메서드 (필수 필드용)
    public static Attendance createNew(LocalDate attendanceDate, Long userId) {
        return Attendance.builder()
                .attendanceDate(attendanceDate)
                .userId(userId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    // 비즈니스 메서드들
    
    /**
     * 출석 처리 (체크인)
     */
    public void doCheckIn(LocalDateTime checkInTime) {
        this.checkIn = checkInTime;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 퇴실 처리 (체크아웃)
     */
    public void doCheckOut(LocalDateTime checkOutTime) {
        this.checkOut = checkOutTime;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 근무 시간 계산 (시간 단위)
     */
    public double getWorkingHours() {
        if (checkIn == null || checkOut == null) {
            return 0.0;
        }
        
        long minutes = java.time.Duration.between(checkIn, checkOut).toMinutes();
        return minutes / 60.0;
    }
    
    /**
     * 4시간 이상 근무했는지 확인
     */
    public boolean isWorkedMoreThanFourHours() {
        return getWorkingHours() >= 4.0;
    }
    
    /**
     * 출석만 하고 퇴실하지 않은 상태인지 확인
     */
    public boolean isCheckedInOnly() {
        return checkIn != null && checkOut == null;
    }
    
    /**
     * 출석도 퇴실도 하지 않은 상태인지 확인
     */
    public boolean isNotProcessed() {
        return checkIn == null && checkOut == null;
    }
}