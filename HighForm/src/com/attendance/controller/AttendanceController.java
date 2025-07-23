package com.attendance.controller;

import com.attendance.model.Attendance;
import com.attendance.service.AttendanceCheckResult;
import com.attendance.service.AttendanceCodeService;
import com.attendance.service.AttendanceService;
import com.attendance.service.exception.AttendanceServiceException;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(Connection connection, AttendanceCodeService codeService) {
        this.attendanceService = new AttendanceService(connection, codeService);
    }

    /**
     * 출석 가능 여부 확인
     */
    public AttendanceCheckResult checkAttendancePossible(Long userId) {
        System.out.println("[Controller] checkAttendancePossible 호출, userId = " + userId);

    	return attendanceService.canCheckIn(userId);
    }

    /**
     * 출석 처리
     */
    public Attendance checkIn(Long userId, String attendanceCode) throws AttendanceServiceException {
        return attendanceService.checkIn(userId, attendanceCode);
    }

    /**
     * 퇴실 가능 여부 확인
     */
    public AttendanceCheckResult checkCheckoutPossible(Long userId) {
        return attendanceService.canCheckOut(userId);
    }

    /**
     * 퇴실 처리
     */
    public Attendance checkOut(Long userId) throws AttendanceServiceException {
        return attendanceService.checkOut(userId);
    }

    /**
     * 오늘 출석 조회
     */
    public Optional<Attendance> getTodayAttendance(Long userId) throws AttendanceServiceException {
        return attendanceService.getTodayAttendance(userId);
    }

    /**
     * 출석 이력 조회
     */
//    public List<Attendance> getAttendanceHistory(Long userId, LocalDate startDate, LocalDate endDate) throws AttendanceServiceException {
//        return attendanceService.getAttendanceHistory(userId, startDate, endDate);
//    }
}
