package com.attendance.service;

import com.attendance.dao.AttendanceDao;
import com.attendance.model.AttendanceApprovalRequest;
import com.attendance.model.enums.ApprovalStatus;
import com.attendance.model.enums.UserRole;
import com.attendance.service.exception.AttendanceServiceException;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class AttendanceApprovalService {
    
    private final AttendanceDao attendanceDao;
    
    public AttendanceApprovalService(Connection connection) {
        this.attendanceDao = new AttendanceDao(connection);
    }
    
    // ============ 승인 요청 관리 ============
    
    /**
     * 휴가/병가 승인 요청
     * @param userId 사용자 ID
     * @param reason 사유
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @param proofFile 증빙 파일 (선택사항)
     * @return 생성된 승인 요청
     */
    public AttendanceApprovalRequest submitApprovalRequest(Long userId, String reason, 
            LocalDate startDate, LocalDate endDate, String proofFile) throws AttendanceServiceException {
        
        try {
            // 1. 날짜 유효성 검증
            if (startDate.isAfter(endDate)) {
                throw new AttendanceServiceException("시작 날짜가 종료 날짜보다 늦을 수 없습니다.");
            }
            
            if (startDate.isBefore(LocalDate.now())) {
                throw new AttendanceServiceException("과거 날짜에 대한 승인 요청은 불가능합니다.");
            }
            
            // 2. 승인 요청 생성
            AttendanceApprovalRequest request = new AttendanceApprovalRequest(userId, reason, startDate, endDate, proofFile);
            
            // 3. DB 저장
            attendanceDao.insertApprovalRequest(request);
            
            return request;
            
        } catch (SQLException e) {
            throw new AttendanceServiceException("승인 요청 처리 중 데이터베이스 오류가 발생했습니다.", e);
        }
    }
    
    /**
     * 승인 요청 승인
     * @param requestId 요청 ID
     * @param approverId 승인자 ID
     * @param approverRole 승인자 권한
     */
    public void approveRequest(Long requestId, Long approverId, UserRole approverRole) throws AttendanceServiceException {
        try {
            // 1. 승인 권한 확인
            if (!canApprove(approverRole)) {
                throw new AttendanceServiceException("승인 권한이 없습니다.");
            }
            
            // 2. 요청 존재 여부 확인
//            Optional<AttendanceApprovalRequest> requestOpt = attendanceDao.findApprovalRequestById(requestId);
//            if (requestOpt.isEmpty()) {
//                throw new AttendanceServiceException("존재하지 않는 승인 요청입니다.");
//            }
//            
//            AttendanceApprovalRequest request = requestOpt.get();
//            
//            // 3. 진행 중인 요청인지 확인
//            if (!request.isPending()) {
//                throw new AttendanceServiceException("이미 처리된 승인 요청입니다.");
//            }
            
            // 4. 승인 처리
            attendanceDao.updateApprovalStatus(requestId, ApprovalStatus.APPROVE, approverId);
            
        } catch (SQLException e) {
            throw new AttendanceServiceException("승인 처리 중 데이터베이스 오류가 발생했습니다.", e);
        }
    }
    
    /**
     * 승인 요청 거절
     * @param requestId 요청 ID
     * @param approverId 승인자 ID
     * @param approverRole 승인자 권한
     */
    public void rejectRequest(Long requestId, Long approverId, UserRole approverRole) throws AttendanceServiceException {
        try {
            // 1. 승인 권한 확인
            if (!canApprove(approverRole)) {
                throw new AttendanceServiceException("승인 권한이 없습니다.");
            }
            
            // 2. 요청 존재 여부 확인
//            Optional<AttendanceApprovalRequest> requestOpt = attendanceDao.findApprovalRequestById(requestId);
//            if (requestOpt.isEmpty()) {
//                throw new AttendanceServiceException("존재하지 않는 승인 요청입니다.");
//            }
//            
//            AttendanceApprovalRequest request = requestOpt.get();
//            
//            // 3. 진행 중인 요청인지 확인
//            if (!request.isPending()) {
//                throw new AttendanceServiceException("이미 처리된 승인 요청입니다.");
//            }
            
            // 4. 거절 처리
            attendanceDao.updateApprovalStatus(requestId, ApprovalStatus.REJECT, approverId);
            
        } catch (SQLException e) {
            throw new AttendanceServiceException("거절 처리 중 데이터베이스 오류가 발생했습니다.", e);
        }
    }
    
    /**
     * 승인 권한 확인
     * @param userRole 사용자 역할
     * @return 승인 권한 여부
     */
    private boolean canApprove(UserRole userRole) {
        return userRole == UserRole.MANAGER || userRole == UserRole.PROFESSOR;
    }
    
    // ============ 조회 기능 ============
    
    /**
     * 사용자별 승인 요청 목록 조회
     * @param userId 사용자 ID
     * @return 승인 요청 목록
     */
//    public List<AttendanceApprovalRequest> getUserApprovalRequests(Long userId) throws AttendanceServiceException {
//        try {
//            return attendanceDao.findApprovalRequestsByUser(userId);
//        } catch (SQLException e) {
//            throw new AttendanceServiceException("승인 요청 목록 조회 중 오류가 발생했습니다.", e);
//        }
//    }
    
    /**
     * 대기 중인 승인 요청 목록 조회 (관리자용)
     * @return 대기 중인 승인 요청 목록
     */
//    public List<AttendanceApprovalRequest> getPendingApprovalRequests() throws AttendanceServiceException {
//        try {
//            return attendanceDao.findPendingApprovalRequests();
//        } catch (SQLException e) {
//            throw new AttendanceServiceException("대기 중인 승인 요청 조회 중 오류가 발생했습니다.", e);
//        }
//    }
    
    /**
     * 특정 날짜의 승인된 요청 확인
     * @param userId 사용자 ID
     * @param date 확인할 날짜
     * @return 승인된 요청 (없으면 Optional.empty())
     */
    public Optional<AttendanceApprovalRequest> getApprovedRequestByDate(Long userId, LocalDate date) throws AttendanceServiceException {
        try {
            return attendanceDao.findApprovedRequestByDate(userId, date);
        } catch (SQLException e) {
            throw new AttendanceServiceException("승인된 요청 조회 중 오류가 발생했습니다.", e);
        }
    }
}