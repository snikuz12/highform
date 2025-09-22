package com.attendance.model.enums;

public enum AttendanceStatus {
    PRESENT("정상출석"),      // 09:00 이전 출석
    LATE("지각"),           // 09:00~14:00 출석
    ABSENT("결석"),         // 14:00 이후 출석 또는 미출석, 4시간 미만 근무
    EXCUSED("승인된 휴가"),   // 승인된 휴가/병가
    EARLY_LEAVE("조퇴");     // 향후 확장용 (현재 로직에서는 사용 안함)
    
    private final String description;
    
    AttendanceStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}