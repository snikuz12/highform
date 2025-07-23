// com/attendance/service/AttendanceService.java
package com.attendance.service;

import com.attendance.dao.AttendanceDao;
import com.attendance.model.Attendance;
import com.attendance.model.AttendanceApprovalRequest;
import com.attendance.model.enums.AttendanceStatus;
import com.attendance.service.exception.AttendanceServiceException;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public class AttendanceService {
    
    private final AttendanceDao attendanceDao;
    private final AttendanceCodeService codeService;
    
    // 출석 시간 상수
    private static final LocalTime LATE_TIME = LocalTime.of(9, 0);      // 09:00 (지각 기준)
    private static final LocalTime ABSENT_TIME = LocalTime.of(14, 0);   // 14:00 (결석 기준)
    private static final LocalTime CUTOFF_TIME = LocalTime.of(23, 0);   // 23:00 (마감 시간)
    private static final double MIN_WORK_HOURS = 4.0;                   // 최소 근무 시간
    
    private AttendanceService(AttendanceDao attendanceDao, AttendanceCodeService codeService) {
        this.attendanceDao = attendanceDao;
        this.codeService = codeService;
    }
    public static AttendanceService createInstance(Connection conn, AttendanceCodeService codeService) {
        return new AttendanceService(new AttendanceDao(conn), codeService);
    }
    
    public AttendanceService(Connection connection, AttendanceCodeService codeService) {
        this.attendanceDao = new AttendanceDao(connection);
        this.codeService = codeService;
    }
    
    // ============ 출석 처리 ============
    
    /**
     * 출석 처리
     * @param userId 사용자 ID
     * @param attendanceCode 출석 코드
     * @return 출석 기록
     * @throws AttendanceServiceException 출석 처리 실패 시
     */
    public Attendance checkIn(Long userId, String attendanceCode) throws AttendanceServiceException {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        LocalTime currentTime = now.toLocalTime();
        
        try {
            // 1. 시간 제한 확인 (23:00 이후 불가)
            if (currentTime.isAfter(CUTOFF_TIME)) {
                throw new AttendanceServiceException("출석 시간이 종료되었습니다. (마감: 23:00)");
            }
            
            // 2. 출석 코드 검증
            if (!codeService.validateTodayCode(attendanceCode)) {
                throw new AttendanceServiceException("잘못된 출석 코드입니다.");
            }
            
            // 3. 중복 출석 확인
            if (attendanceDao.existsTodayAttendance(userId, today)) {
                throw new AttendanceServiceException("이미 출석 처리되었습니다.");
            }
            
            // 4. 승인된 휴가/병가 확인
            Optional<AttendanceApprovalRequest> approvedRequest = 
                attendanceDao.findApprovedRequestByDate(userId, today);
            
            AttendanceStatus status;
            if (approvedRequest.isPresent()) {
                status = AttendanceStatus.EXCUSED;
            } else {
                status = determineAttendanceStatus(currentTime);
            }
            
            // 5. 출석 기록 생성 및 저장
            Attendance attendance = new Attendance(userId, today, now, status);
            attendanceDao.insertAttendance(attendance);
            
            return attendance;
            
        } catch (SQLException e) {
            throw new AttendanceServiceException("출석 처리 중 데이터베이스 오류가 발생했습니다.", e);
        }
    }
    
    /**
     * 퇴실 처리
     * @param userId 사용자 ID
     * @return 업데이트된 출석 기록
     * @throws AttendanceServiceException 퇴실 처리 실패 시
     */
    public Attendance checkOut(Long userId) throws AttendanceServiceException {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        LocalTime currentTime = now.toLocalTime();
        
        try {
            // 1. 시간 제한 확인 (23:00 이후 불가)
            if (currentTime.isAfter(CUTOFF_TIME)) {
                throw new AttendanceServiceException("퇴실 시간이 종료되었습니다. (마감: 23:00)");
            }
            
            // 2. 당일 출석 기록 확인
            Optional<Attendance> attendanceOpt = attendanceDao.findTodayAttendance(userId, today);
            if (attendanceOpt.isEmpty()) {
                throw new AttendanceServiceException("출석 기록이 없습니다.");
            }
            
            Attendance attendance = attendanceOpt.get();
            
            // 3. 퇴실 가능 여부 확인
            if (!attendance.canCheckOut()) {
                throw new AttendanceServiceException("퇴실 처리가 불가능합니다. (이미 퇴실했거나 출석 기록이 없음)");
            }
            
            // 4. 퇴실 처리 및 상태 업데이트
            attendance.processCheckOut(now);
            
            // 5. DB 업데이트
            attendanceDao.updateCheckOut(userId, today, now, attendance.getStatus());
            
            return attendance;
            
        } catch (SQLException e) {
            throw new AttendanceServiceException("퇴실 처리 중 데이터베이스 오류가 발생했습니다.", e);
        }
    }
    
    /**
     * 출석 상태 결정
     * @param checkInTime 출석 시간
     * @return 출석 상태
     */
    private AttendanceStatus determineAttendanceStatus(LocalTime checkInTime) {
        if (checkInTime.isBefore(LATE_TIME)) {
            return AttendanceStatus.PRESENT;    // 09:00 이전 - 정상출석
        } else if (checkInTime.isBefore(ABSENT_TIME)) {
            return AttendanceStatus.LATE;       // 09:00~14:00 - 지각
        } else {
            return AttendanceStatus.ABSENT;     // 14:00 이후 - 결석
        }
    }
    
    // ============ 조회 기능 ============
    
    /**
     * 당일 출석 현황 조회
     * @param userId 사용자 ID
     * @return 출석 기록 (없으면 Optional.empty())
     */
    public Optional<Attendance> getTodayAttendance(Long userId) throws AttendanceServiceException {
        try {
            return attendanceDao.findTodayAttendance(userId, LocalDate.now());
        } catch (SQLException e) {
            throw new AttendanceServiceException("출석 현황 조회 중 오류가 발생했습니다.", e);
        }
    }
    
    /**
     * 기간별 출석 기록 조회
     * @param userId 사용자 ID
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 출석 기록 목록
     */
//    public List<Attendance> getAttendanceHistory(Long userId, LocalDate startDate, LocalDate endDate) 
//            throws AttendanceServiceException {
//        try {
//            return attendanceDao.findAttendanceByPeriod(userId, startDate, endDate);
//        } catch (SQLException e) {
//            throw new AttendanceServiceException("출석 기록 조회 중 오류가 발생했습니다.", e);
//        }
//    }
    
    /**
     * 출석 가능 여부 확인
     * @param userId 사용자 ID
     * @return 출석 가능 여부와 메시지
     */
    public AttendanceCheckResult canCheckIn(Long userId) {
        System.out.println("[AttendanceService] canCheckIn 호출됨, userId = " + userId);

    	
    	LocalTime currentTime = LocalTime.now();
        LocalDate today = LocalDate.now();
        
        try {
            // 시간 제한 확인
            if (currentTime.isAfter(CUTOFF_TIME)) {
                return new AttendanceCheckResult(false, "출석 시간이 종료되었습니다. (마감: 23:00)");
            }
            
            // 중복 출석 확인
            if (attendanceDao.existsTodayAttendance(userId, today)) {
                return new AttendanceCheckResult(false, "이미 출석 처리되었습니다.");
            }
            
            return new AttendanceCheckResult(true, "출석 가능합니다.");
            
        } catch (SQLException e) {
            System.out.println("[AttendanceService] DB 오류: " + e.getMessage());

            return new AttendanceCheckResult(false, "출석 가능 여부 확인 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 퇴실 가능 여부 확인
     * @param userId 사용자 ID
     * @return 퇴실 가능 여부와 메시지
     */
    public AttendanceCheckResult canCheckOut(Long userId) {
        LocalTime currentTime = LocalTime.now();
        LocalDate today = LocalDate.now();
        
        try {
            // 시간 제한 확인
            if (currentTime.isAfter(CUTOFF_TIME)) {
                return new AttendanceCheckResult(false, "퇴실 시간이 종료되었습니다. (마감: 23:00)");
            }
            
            // 출석 기록 확인
            Optional<Attendance> attendanceOpt = attendanceDao.findTodayAttendance(userId, today);
            if (attendanceOpt.isEmpty()) {
                return new AttendanceCheckResult(false, "출석 기록이 없습니다.");
            }
            
            Attendance attendance = attendanceOpt.get();
            if (!attendance.canCheckOut()) {
                return new AttendanceCheckResult(false, "이미 퇴실 처리되었습니다.");
            }
            
            return new AttendanceCheckResult(true, "퇴실 가능합니다.");
            
        } catch (SQLException e) {
            return new AttendanceCheckResult(false, "퇴실 가능 여부 확인 중 오류가 발생했습니다.");
        }
    }
}