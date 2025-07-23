package com.attendance.dao;

import com.attendance.dao.AttendanceSql;
import com.attendance.model.Attendance;
import com.attendance.model.AttendanceApprovalRequest;
import com.attendance.model.enums.AttendanceStatus;
import com.attendance.model.enums.ApprovalStatus;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AttendanceDao {
    
    private Connection connection;
    
    public AttendanceDao(Connection connection) {
        this.connection = connection;
    }
    
    // ============ 출석 관련 메서드 ============
    
    /**
     * 출석 기록 저장
     */
    public void insertAttendance(Attendance attendance) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(AttendanceSql.INSERT_ATTENDANCE)) {
            pstmt.setDate(1, Date.valueOf(attendance.getAttendanceDate()));
            pstmt.setTimestamp(2, Timestamp.valueOf(attendance.getCheckIn()));
            pstmt.setString(3, attendance.getStatus().name());
            pstmt.setLong(4, attendance.getUserId());
            
            pstmt.executeUpdate();
        }
    }
    
    /**
     * 당일 출석 기록 조회
     */
    public Optional<Attendance> findTodayAttendance(Long userId, LocalDate date) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(AttendanceSql.SELECT_TODAY_ATTENDANCE)) {
            pstmt.setLong(1, userId);
            pstmt.setDate(2, Date.valueOf(date));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAttendance(rs));
                }
            }
        }
        return Optional.empty();
    }
    
    /**
     * 퇴실 처리
     */
    public void updateCheckOut(Long userId, LocalDate date, LocalDateTime checkOutTime, AttendanceStatus status) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(AttendanceSql.UPDATE_CHECK_OUT)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(checkOutTime));
            pstmt.setString(2, status.name());
            pstmt.setLong(3, userId);
            pstmt.setDate(4, Date.valueOf(date));
            
            pstmt.executeUpdate();
        }
    }
    

    
    /**
     * 중복 출석 확인
     */
    public boolean existsTodayAttendance(Long userId, LocalDate date) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(AttendanceSql.COUNT_TODAY_ATTENDANCE)) {
            pstmt.setLong(1, userId);
            pstmt.setDate(2, Date.valueOf(date));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
    
    /**
     * 당일 승인된 휴가/병가 확인         
     */
    public Optional<AttendanceApprovalRequest> findApprovedRequestByDate(Long userId, LocalDate date) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(AttendanceSql.SELECT_APPROVED_REQUEST_BY_DATE)) {
            pstmt.setLong(1, userId);
            pstmt.setDate(2, Date.valueOf(date));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToApprovalRequest(rs));
                }
            }
        }
        return Optional.empty();
    }
    // ==========================배치나 통계처리용==================================================
    /**
     * 당일 퇴실하지 않은 출석자 조회 (배치용)                      
     */
    public List<Attendance> findUnprocessedAttendance(LocalDate date) throws SQLException {
        List<Attendance> attendances = new ArrayList<>();
        
        try (PreparedStatement pstmt = connection.prepareStatement(AttendanceSql.SELECT_UNPROCESSED_ATTENDANCE)) {
            pstmt.setDate(1, Date.valueOf(date));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    attendances.add(mapResultSetToAttendance(rs));
                }
            }
        }
        
        return attendances;
    }
    
    /**
     * 자동 결석 처리 (배치용)                      
     */
    public void updateAutoAbsent(Long attendanceId, LocalDateTime checkOutTime) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(AttendanceSql.UPDATE_AUTO_ABSENT)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(checkOutTime));
            pstmt.setLong(2, attendanceId);
            
            pstmt.executeUpdate();
        }
    }
    
    /**
     * 기간별 출석 기록 조회       
     */
    public List<Attendance> findAttendanceByPeriod(Long userId, LocalDate startDate, LocalDate endDate) throws SQLException {
        List<Attendance> attendances = new ArrayList<>();
        
        try (PreparedStatement pstmt = connection.prepareStatement(AttendanceSql.SELECT_ATTENDANCE_BY_PERIOD)) {
            pstmt.setLong(1, userId);
            pstmt.setDate(2, Date.valueOf(startDate));
            pstmt.setDate(3, Date.valueOf(endDate));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    attendances.add(mapResultSetToAttendance(rs));
                }
            }
        }
        
        return attendances;
    }
    
    // ============ 승인 요청 관련 메서드 ============
    
    /**
     * 승인 요청 저장
     */
    public void insertApprovalRequest(AttendanceApprovalRequest request) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(AttendanceSql.INSERT_APPROVAL_REQUEST)) {
            pstmt.setString(1, request.getReason());
            pstmt.setString(2, request.getProofFile());
            pstmt.setDate(3, Date.valueOf(request.getStartDate()));
            pstmt.setDate(4, Date.valueOf(request.getEndDate()));
            pstmt.setLong(5, request.getUserId());
            
            pstmt.executeUpdate();
        }
    }
    

    
    /**
     * 승인 상태 업데이트
     */
    public void updateApprovalStatus(Long requestId, ApprovalStatus status, Long approverId) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(AttendanceSql.UPDATE_APPROVAL_STATUS)) {
            pstmt.setString(1, status.name().toLowerCase());
            pstmt.setLong(2, approverId);
            pstmt.setLong(3, requestId);
            
            pstmt.executeUpdate();
        }
    }
    
    /**
     * 사용자별 승인 요청 목록 조회
     */
//    public List<AttendanceApprovalRequest> findApprovalRequestsByUser(Long userId) throws SQLException {
//        List<AttendanceApprovalRequest> requests = new ArrayList<>();
//        
//        try (PreparedStatement pstmt = connection.prepareStatement(AttendanceSql.SELECT_APPROVAL_REQUESTS_BY_USER)) {
//            pstmt.setLong(1, userId);
//            
//            try (ResultSet rs = pstmt.executeQuery()) {
//                while (rs.next()) {
//                    requests.add(mapResultSetToApprovalRequest(rs));
//                }
//            }
//        }
//        
//        return requests;
//    }
    
    /**
     * 대기중인 승인 요청 목록 조회 (관리자용)
     */
//    public List<AttendanceApprovalRequest> findPendingApprovalRequests() throws SQLException {
//        List<AttendanceApprovalRequest> requests = new ArrayList<>();
//        
//        try (PreparedStatement pstmt = connection.prepareStatement(AttendanceSql.SELECT_PENDING_APPROVAL_REQUESTS)) {
//            try (ResultSet rs = pstmt.executeQuery()) {
//                while (rs.next()) {
//                    requests.add(mapResultSetToApprovalRequest(rs));
//                }
//            }
//        }
//        
//        return requests;
//    }
    
    /**
     * 특정 승인 요청 조회
     */
//    public Optional<AttendanceApprovalRequest> findApprovalRequestById(Long requestId) throws SQLException {
//        try (PreparedStatement pstmt = connection.prepareStatement(AttendanceSql.SELECT_APPROVAL_REQUEST_BY_ID)) {
//            pstmt.setLong(1, requestId);
//            
//            try (ResultSet rs = pstmt.executeQuery()) {
//                if (rs.next()) {
//                    return Optional.of(mapResultSetToApprovalRequest(rs));
//                }
//            }
//        }
//        return Optional.empty();
//    }
    
    // ============ ResultSet 매핑 메서드 ============
    
    /**
     * ResultSet을 Attendance 객체로 변환
     */
    private Attendance mapResultSetToAttendance(ResultSet rs) throws SQLException {
        Attendance attendance = new Attendance();
        attendance.setId(rs.getLong("id"));
        attendance.setAttendanceDate(rs.getDate("attendance_date").toLocalDate());
        
        Timestamp checkInTs = rs.getTimestamp("check_in");
        if (checkInTs != null) {
            attendance.setCheckIn(checkInTs.toLocalDateTime());
        }
        
        Timestamp checkOutTs = rs.getTimestamp("check_out");
        if (checkOutTs != null) {
            attendance.setCheckOut(checkOutTs.toLocalDateTime());
        }
        
        String statusStr = rs.getString("status");
        if (statusStr != null) {
            attendance.setStatus(AttendanceStatus.valueOf(statusStr));
        }
        
        attendance.setUserId(rs.getLong("user_id"));
        attendance.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        attendance.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        
        return attendance;
    }
    
    /**
     * ResultSet을 AttendanceApprovalRequest 객체로 변환
     */
    private AttendanceApprovalRequest mapResultSetToApprovalRequest(ResultSet rs) throws SQLException {
        AttendanceApprovalRequest request = new AttendanceApprovalRequest();
        request.setId(rs.getLong("id"));
        request.setReason(rs.getString("reason"));
        request.setProofFile(rs.getString("proof_file"));
        
        String statusStr = rs.getString("status");
        if (statusStr != null) {
            request.setStatus(ApprovalStatus.valueOf(statusStr.toUpperCase()));
        }
        
        Timestamp requestedAtTs = rs.getTimestamp("requested_at");
        if (requestedAtTs != null) {
            request.setRequestedAt(requestedAtTs.toLocalDateTime());
        }
        
        Timestamp decisionAtTs = rs.getTimestamp("decision_at");
        if (decisionAtTs != null) {
            request.setDecisionAt(decisionAtTs.toLocalDateTime());
        }
        
        Date startDate = rs.getDate("start_date");
        if (startDate != null) {
            request.setStartDate(startDate.toLocalDate());
        }
        
        Date endDate = rs.getDate("end_date");
        if (endDate != null) {
            request.setEndDate(endDate.toLocalDate());
        }
        
        request.setUserId(rs.getLong("user_id"));
        
        long approverId = rs.getLong("approver_id");
        if (!rs.wasNull()) {
            request.setApproverId(approverId);
        }
        
        return request;
    }
}