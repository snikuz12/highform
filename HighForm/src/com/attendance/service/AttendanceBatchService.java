package com.attendance.service;

import com.attendance.dao.AttendanceDao;
import com.attendance.model.Attendance;
import com.attendance.service.exception.AttendanceServiceException;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 배치 처리용 서비스
 * 매일 23:00에 실행되어 퇴실하지 않은 출석자를 자동으로 결석 처리
 */
public class AttendanceBatchService {
    
    private final AttendanceDao attendanceDao;
    private static final LocalTime CUTOFF_TIME = LocalTime.of(23, 0); // 23:00
    
    public AttendanceBatchService(Connection connection) {
        this.attendanceDao = new AttendanceDao(connection);
    }
    
    /**
     * 자동 결석 처리 배치 작업
     * @param targetDate 처리할 날짜 (일반적으로 당일)
     * @return 처리된 출석 기록 수
     */
    public int processAutoAbsent(LocalDate targetDate) throws AttendanceServiceException {
        try {
            // 1. 당일 퇴실하지 않은 출석자 조회
            List<Attendance> unprocessedAttendances = attendanceDao.findUnprocessedAttendance(targetDate);
            
            int processedCount = 0;
            LocalDateTime cutoffDateTime = LocalDateTime.of(targetDate, CUTOFF_TIME);
            
            // 2. 각 출석자에 대해 자동 결석 처리
            for (Attendance attendance : unprocessedAttendances) {
                // 승인된 휴가/병가는 처리하지 않음
                if (attendance.getStatus().equals(com.attendance.model.enums.AttendanceStatus.EXCUSED)) {
                    continue;
                }
                
                // 자동 퇴실 및 결석 처리
                attendanceDao.updateAutoAbsent(attendance.getId(), cutoffDateTime);
                processedCount++;
            }
            
            return processedCount;
            
        } catch (SQLException e) {
            throw new AttendanceServiceException("자동 결석 처리 중 데이터베이스 오류가 발생했습니다.", e);
        }
    }
    
    /**
     * 오늘 자동 결석 처리
     * @return 처리된 출석 기록 수
     */
    public int processTodayAutoAbsent() throws AttendanceServiceException {
        return processAutoAbsent(LocalDate.now());
    }
}



