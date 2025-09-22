package com.attendance.service.exception;

/**
 * 출석 서비스 관련 예외
 */
public class AttendanceServiceException extends Exception {
    
    public AttendanceServiceException(String message) {
        super(message);
    }
    
    public AttendanceServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}