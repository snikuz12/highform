package com.attendance.service;

/**
 * 출석/퇴실 가능 여부 확인 결과
 */
public class AttendanceCheckResult {
    private final boolean possible;
    private final String message;
    
    public AttendanceCheckResult(boolean possible, String message) {
        this.possible = possible;
        this.message = message;
    }
    
    public boolean isPossible() {
        return possible;
    }
    
    public String getMessage() {
        return message;
    }
    
    @Override
    public String toString() {
        return "AttendanceCheckResult{" +
                "possible=" + possible +
                ", message='" + message + '\'' +
                '}';
    }
}
